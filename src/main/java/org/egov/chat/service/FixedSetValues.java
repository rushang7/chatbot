package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.models.ConversationState;
import org.egov.chat.repository.ConversationStateRepository;
import org.egov.chat.service.valuefetch.ValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class FixedSetValues {

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
            nextSet.add(allValues.get(i));
        }

        if(upperLimit < allValues.size())
            nextSet.add(nextKeyword);

        ( (ObjectNode) questionDetails ).put("offset", newOffset);
        ( (ObjectNode) questionDetails ).set("askedValues", nextSet);

        return questionDetails;
    }

    public JsonNode extractAnswer(JsonNode config, JsonNode chatNode) {
        boolean displayValuesAsOptions = config.get("displayValuesAsOptions") != null && config.get("displayValuesAsOptions").asBoolean();

        String answer = chatNode.at(JsonPointerNameConstants.messageContent).asText();
        ConversationState conversationState = getConversationStateForChat(chatNode);
        JsonNode questionDetails = conversationState.getQuestionDetails();

        List<String> validValues = null;

        try {
            if(displayValuesAsOptions)
                validValues = objectMapper.readValue(questionDetails.get("askedValues").toString(), List.class);
            else
                validValues = objectMapper.readValue(questionDetails.get("allValues").toString(), List.class);
        } catch (IOException e) {
            return null;
        }


        Integer answerIndex;
        if(displayValuesAsOptions && checkIfAnswerIsIndex(answer)) {
            answerIndex = Integer.parseInt(answer) - 1;
        } else {
            Integer highestFuzzyScoreMatch = 0;
            answerIndex = 0;
            for(int i = 0; i < validValues.size(); i++) {
                Integer score = FuzzySearch.tokenSetRatio(validValues.get(i), answer);
                if(score > highestFuzzyScoreMatch) {
                    highestFuzzyScoreMatch = score;
                    answerIndex = i;
                }
            }
        }

        String finalAnswer = validValues.get(answerIndex);

        if(finalAnswer.equalsIgnoreCase(nextKeyword)) {
            ( (ObjectNode) chatNode).put("reQuestion", true);
        } else {
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
            if(displayValuesAsOptions)
                validValues = objectMapper.readValue(questionDetails.get("askedValues").toString(), List.class);
            else
                validValues = objectMapper.readValue(questionDetails.get("allValues").toString(), List.class);
        } catch (IOException e) {
            return false;
        }

        if(displayValuesAsOptions && checkIfAnswerIsIndex(answer)) {
            return checkIfIndexIsValid(answer, validValues);
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
            fuzzyMatchScore = FuzzySearch.tokenSetRatio(answer, validValue);
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
