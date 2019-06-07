package org.egov.chat.service.validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.egov.chat.service.FixedSetValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Validator {

    @Autowired
    private TypeValidator typeValidator;
    @Autowired
    private FixedSetValues fixedSetValues;

    public boolean isValid(JsonNode config, JsonNode chatNode) {

        if(! (config.get("validationRequired") != null && config.get("validationRequired").asText()
                .equalsIgnoreCase("true")))
            return true;

        if(! typeValidator.isValid(config, chatNode))
            return false;

        if(config.get("validationRequired") != null && config.get("validationRequired").asText().equalsIgnoreCase("true")) {
            if (config.get("typeOfValues") != null) {
                String validatorType = config.get("typeOfValues").asText();
                if (validatorType.equalsIgnoreCase("FixedSetValues"))
                    return fixedSetValues.isValid(config, chatNode);
            }
        }
        return true;
    }

}
