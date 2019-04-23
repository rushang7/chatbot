package org.egov.chat.service.validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Validator {

    @Autowired
    private FixedSetValueValidator fixedSetValueValidator;

    public boolean isValid(JsonNode config, JsonNode chatNode) {

        if(config.get("validationRequired") != null && config.get("validationRequired").asText().equalsIgnoreCase("true")) {
            String validatorType = config.get("validatorType").asText();
            if (validatorType.equalsIgnoreCase("FixedSetValues"))
                return fixedSetValueValidator.isValid(config, chatNode);
        }

        return true;
    }

}
