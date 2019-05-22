package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
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

    public JsonNode getQuestion(JsonNode config, JsonNode chatNode) {
        ObjectNode nodeWithQuestion = chatNode.deepCopy();

        String question = getQuesitonForConfig(config);
        question += getOptionsForConfig(config, chatNode);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "text");
        response.put("text", question);

        nodeWithQuestion.set("response", response);
        return nodeWithQuestion;
    }

    private String getQuesitonForConfig(JsonNode config) {
        return config.get("message").asText();
    }

    private String getOptionsForConfig(JsonNode config, JsonNode chatNode) {
        String options = "";

        if(config.get("validatorType") != null && config.get("validatorType").asText().equalsIgnoreCase("FixedSetValues")) {
            if(config.get("displayValuesAsOptions") != null && config.get("displayValuesAsOptions").asText().equalsIgnoreCase("true")) {
                List<String> values = valueFetcher.getAllValidValues(config, chatNode);
                for(int index = 1; index < values.size() + 1; index++) {
                    options += "\n" + index + ". " + values.get(index - 1);
                }
            }
        }

        return options;
    }

}
