package org.egov.chat.service.validation;

import com.fasterxml.jackson.databind.JsonNode;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.service.valuefetch.ValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FixedSetValueValidator {

    @Autowired
    private ValueFetcher valueFetcher;

    public boolean isValid(JsonNode config, JsonNode chatNode) {
        boolean displayValuesAsOptions = config.get("displayValuesAsOptions") != null ?
                config.get("displayValuesAsOptions").asBoolean() : false;
        String answer = chatNode.at(JsonPointerNameConstants.messageContent).asText();
        List<String> validValues = valueFetcher.getAllValidValues(config, chatNode);

        if(displayValuesAsOptions && checkIfAnswerIsIndex(answer)) {
            return checkIfIndexIsValid(answer, validValues);
        } else {
            return fuzzyMatchAnswerWithValidValues(answer, validValues, config);
        }
    }

    boolean checkIfAnswerIsIndex(String answer) {
        try {
            Integer.parseInt(answer);
            return true;
        } catch (NumberFormatException exception) {
            return false;
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

        Integer matchScoreThreshold = config.get("fuzzyMatch").asInt();

        Integer fuzzyMatchScore;
        for(String validValue : validValues) {
            fuzzyMatchScore = FuzzySearch.tokenSetRatio(answer, validValue);
            if(fuzzyMatchScore >= matchScoreThreshold)
                return true;
        }
        return false;
    }

}
