package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.models.ConversationState;
import org.egov.chat.repository.ConversationStateRepository;
import org.egov.chat.service.valuefetch.ValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class FixedSetValues {

    private String nextKeywordSymbol = "0";
    private String nextKeyword = "Next";

    @Autowired
    private ValueFetcher valueFetcher;
    @Autowired
    private ConversationStateRepository conversationStateRepository;
    @Autowired
    private ObjectMapper objectMapper;


    public JsonNode getAllValidValues(JsonNode config, JsonNode chatNode) {
        ObjectNode questionDetails = objectMapper.createObjectNode();
        if(config.get("values").get("batchSize") != null)
            questionDetails.put("batchSize", config.get("values").get("batchSize").asInt());
        else
            questionDetails.put("batchSize", Integer.MAX_VALUE);

        List<String> validValues = valueFetcher.getAllValidValues(config, chatNode);
        ArrayNode values = objectMapper.valueToTree(validValues);
        questionDetails.putArray("allValues").addAll(values);

        return questionDetails;
    }

    public JsonNode getNextSet(JsonNode questionDetails) {
        Integer batchSize = questionDetails.get("batchSize").asInt();
        ArrayNode allValues = (ArrayNode) questionDetails.get("allValues");

        Integer newOffset;
        if(questionDetails.has("offset")) {
            Integer previousOffset = questionDetails.get("offset").asInt();
            if(previousOffset + batchSize > allValues.size())
                return null;
            newOffset = previousOffset + batchSize;
        } else {
            newOffset = 0;
        }

        ArrayNode nextSet = objectMapper.createArrayNode();

        Integer upperLimit = newOffset + batchSize < allValues.size() ? newOffset + batchSize : allValues.size();

        for(int i = newOffset; i < upperLimit; i++) {
            ObjectNode value = objectMapper.createObjectNode();
            value.put("index", i + 1);
            value.set("value", allValues.get(i));
            nextSet.add(value);
        }

        if(upperLimit < allValues.size()) {
            ObjectNode value = objectMapper.createObjectNode();
            value.put("index", nextKeywordSymbol);
            value.put("value", nextKeyword);
            nextSet.add(value);
        }

        ( (ObjectNode) questionDetails ).put("offset", newOffset);
        ( (ObjectNode) questionDetails ).set("askedValues", nextSet);

        return questionDetails;
    }

    public JsonNode extractAnswer(JsonNode config, JsonNode chatNode) {
        boolean displayValuesAsOptions = config.get("displayValuesAsOptions") != null && config.get("displayValuesAsOptions").asBoolean();

        String answer = chatNode.at(JsonPointerNameConstants.messageContent).asText();
        ConversationState conversationState = getConversationStateForChat(chatNode);
        JsonNode questionDetails = conversationState.getQuestionDetails();

        List<String> validValues;
        try {
            validValues = objectMapper.readValue(questionDetails.get("allValues").toString(), List.class);
        } catch (IOException e) {
            return null;
        }

        if(displayValuesAsOptions) {
            Integer offset = questionDetails.get("offset").asInt();
            Integer batchSize = questionDetails.get("batchSize").asInt();
            validValues = validValues.subList(0, Math.min(offset + batchSize, validValues.size()));
        }

        Integer answerIndex = null;
        Boolean reQuestion = false;
        if(displayValuesAsOptions && (answer.equalsIgnoreCase(nextKeyword) || answer.equalsIgnoreCase(nextKeywordSymbol))) {
            reQuestion = true;
        } else if(displayValuesAsOptions && checkIfAnswerIsIndex(answer)) {
            answerIndex = Integer.parseInt(answer) - 1;
        } else {
            Integer highestFuzzyScoreMatch = 0;
            answerIndex = 0;
            for(int i = 0; i < validValues.size(); i++) {
                Integer score = FuzzySearch.tokenSortRatio(validValues.get(i), answer);
                if(score > highestFuzzyScoreMatch) {
                    highestFuzzyScoreMatch = score;
                    answerIndex = i;
                }
            }
        }

        log.debug("Answer Index : " + answerIndex);

        String finalAnswer = null;
        if(reQuestion) {
            ( (ObjectNode) chatNode).put("reQuestion", true);
            finalAnswer = nextKeyword;
        } else {
            finalAnswer = validValues.get(answerIndex);
            finalAnswer = valueFetcher.getCodeForValue(config, chatNode, finalAnswer);
        }

        // TODO : jsonpath
        ( (ObjectNode) chatNode.get("message")).put("content", finalAnswer);

        return chatNode;
    }

    private boolean checkIfAnswerIsIndex(String answer) {
        try {
            Integer.parseInt(answer);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    // TODO : Get Question Details from ChatNode
    private ConversationState getConversationStateForChat(JsonNode chatNode) {
        String conversationId = chatNode.at(JsonPointerNameConstants.conversationId).asText();
        return conversationStateRepository.getConversationStateForId(conversationId);
    }

    public boolean isValid(JsonNode config, JsonNode chatNode) {
        boolean displayValuesAsOptions = config.get("displayValuesAsOptions") != null && config.get("displayValuesAsOptions").asBoolean();
        String answer = chatNode.at(JsonPointerNameConstants.messageContent).asText();

        ConversationState conversationState = getConversationStateForChat(chatNode);
        JsonNode questionDetails = conversationState.getQuestionDetails();

        List<String> validValues;

        try {
            validValues = objectMapper.readValue(questionDetails.get("allValues").toString(), List.class);
        } catch (IOException e) {
            return false;
        }

        if(displayValuesAsOptions && checkIfAnswerIsIndex(answer)) {
            Integer offset = questionDetails.get("offset").asInt();
            Integer batchSize = questionDetails.get("batchSize").asInt();
            validValues = validValues.subList(0, Math.min(offset + batchSize, validValues.size()));
            return checkIfIndexIsValid(answer, validValues);
        } else if(displayValuesAsOptions && (answer.equalsIgnoreCase(nextKeyword) || answer.equalsIgnoreCase(nextKeywordSymbol))) {
            return true;
        } else {
            return fuzzyMatchAnswerWithValidValues(answer, validValues, config);
        }
    }

    boolean checkIfIndexIsValid(String answer, List<String> validValues) {
        Integer answerInteger = Integer.parseInt(answer);
        if(answerInteger > 0 && answerInteger <= validValues.size())
            return true;
        else
            return false;
    }

    boolean fuzzyMatchAnswerWithValidValues(String answer, List<String> validValues, JsonNode config) {

        Integer matchScoreThreshold = config.get("matchAnswerThreshold").asInt();

        Integer fuzzyMatchScore;
        for(String validValue : validValues) {
            fuzzyMatchScore = FuzzySearch.tokenSortRatio(answer, validValue);
            if(fuzzyMatchScore >= matchScoreThreshold)
                return true;
        }
        return false;
    }


    public List<String> fuzzyMatchAnswer(JsonNode config, JsonNode chatNode) {
        return null;
    }

    public String matchAnswer(JsonNode config, JsonNode chatNode) {
        return null;
    }


}
