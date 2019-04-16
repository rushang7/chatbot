package org.egov.chat.validation;

import com.fasterxml.jackson.databind.JsonNode;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.egov.chat.valuefetch.ValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FixedSetValueValidator {

    @Autowired
    private ValueFetcher valueFetcher;

    public boolean isValid(JsonNode config, JsonNode chatNode) {
        String answer = chatNode.get("answer").asText();
        List<String> validValues = valueFetcher.getAllValidValues(config, chatNode);

        if(checkIfAnswerIsIndex(answer)) {
            return checkIfIndexIsValid(answer, validValues);
        } else {
            return fuzzyMatchAnswerWithValidValues(answer, validValues, config);
        }
    }

    public boolean checkIfAnswerIsIndex(String answer) {
        try {
            Integer.parseInt(answer);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public boolean checkIfIndexIsValid(String answer, List<String> validValues) {
        Integer answerInteger = Integer.parseInt(answer);
        if(answerInteger > 0 && answerInteger <= validValues.size())
            return true;
        else
            return false;
    }

    private boolean fuzzyMatchAnswerWithValidValues(String answer, List<String> validValues, JsonNode config) {

        Integer minimumMatchScore = config.get("fuzzyMatch").asInt();

        Integer fuzzyMatchScore;
        for(String validValue : validValues) {
            fuzzyMatchScore = FuzzySearch.ratio(answer, validValue);
            if(fuzzyMatchScore >= minimumMatchScore)
                return true;
        }
        return false;
    }

}
