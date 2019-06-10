package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.repository.ConversationStateRepository;
import org.egov.chat.service.valuefetch.ValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class QuestionGenerator {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ValueFetcher valueFetcher;
    @Autowired
    private FixedSetValues fixedSetValues;
    @Autowired
    private ConversationStateRepository conversationStateRepository;

    public JsonNode getQuestion(JsonNode config, JsonNode chatNode) {
        String question = getQuesitonForConfig(config);
        question += getOptionsForConfig(config, chatNode);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "text");
        response.put("text", question);

        ((ObjectNode) chatNode).set("response", response);
        return chatNode;
    }

    private String getQuesitonForConfig(JsonNode config) {
        return config.get("message").asText();
    }

    private String getOptionsForConfig(JsonNode config, JsonNode chatNode) {
        String options = "";

        if(config.get("typeOfValues") != null && config.get("typeOfValues").asText().equalsIgnoreCase("FixedSetValues")) {

            if(config.get("displayValuesAsOptions") != null && config.get("displayValuesAsOptions").asText().equalsIgnoreCase("true")) {

                boolean reQuestion = chatNode.get("reQuestion") != null && chatNode.get("reQuestion").asBoolean();
                JsonNode questionDetails;
                if(reQuestion) {
                    questionDetails = conversationStateRepository.getConversationStateForId(
                            chatNode.at(JsonPointerNameConstants.conversationId).asText()).getQuestionDetails();
                } else {
                    questionDetails = fixedSetValues.getAllValidValues(config, chatNode);
                }

                questionDetails = fixedSetValues.getNextSet(questionDetails);

                ( (ObjectNode) chatNode).set("questionDetails", questionDetails);

                ArrayNode values = (ArrayNode) questionDetails.get("askedValues");

                for(int index = 1; index < values.size() + 1; index++) {
                    options += "\n" + index + ". " + values.get(index - 1).asText();
                }
            } else {

                JsonNode questionDetails = fixedSetValues.getAllValidValues(config, chatNode);
                ( (ObjectNode) chatNode).set("questionDetails", questionDetails);

            }
        }

        return options;
    }

}
