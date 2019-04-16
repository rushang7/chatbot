package org.egov.chat.valuefetch;

import com.fasterxml.jackson.databind.JsonNode;
import org.egov.chat.xternal.MdmsValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValueFetcher {

//    @Autowired
//    List<ExternalValueFetcher> externalValueFetchers;

    public List<String> getAllValidValues(JsonNode config, JsonNode chatNode) {
        List<String> validValues = new ArrayList<>();

        if(config.get("values").isArray()) {
            validValues = getValuesFromArrayNode(config);
        }
        else if(config.get("values").isObject()) {
            validValues = getValuesFromExternalSource(config, chatNode);
        }

        return validValues;
    }

    List<String> getValuesFromArrayNode(JsonNode config) {
        List<String> validValues = new ArrayList<>();
        for(JsonNode jsonNode : config.get("values")) {
            validValues.add(jsonNode.asText());
        }
        return validValues;
    }

    List<String> getValuesFromExternalSource(JsonNode config, JsonNode chatNode) {



        return null;
    }

}
