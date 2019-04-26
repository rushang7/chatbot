package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.service.valuefetch.ValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerExtractor {

    @Autowired
    private ValueFetcher valueFetcher;

    public JsonNode extractAnswer(JsonNode config, JsonNode chatNode) {

        if(config.get("validatorType") != null && config.get("validatorType").asText().equalsIgnoreCase("FixedSetValues")) {
            chatNode = extractAnswerFromFixedSet(config, chatNode);
        }

        return chatNode;
    }

    private JsonNode extractAnswerFromFixedSet(JsonNode config, JsonNode chatNode) {
        boolean displayValuesAsOptions = config.get("displayValuesAsOptions") != null ?
                config.get("displayValuesAsOptions").asBoolean() : false;

        String answer = chatNode.at(JsonPointerNameConstants.messageContent).asText();
        List<String> validValues = valueFetcher.getAllValidValues(config, chatNode);

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
        ObjectNode objectNode = (ObjectNode) chatNode;
        objectNode.put("answer", finalAnswer);

        return objectNode;
    }

    private boolean checkIfAnswerIsIndex(String answer) {
        try {
            Integer.parseInt(answer);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

}
