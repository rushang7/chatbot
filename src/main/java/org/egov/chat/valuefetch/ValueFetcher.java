package org.egov.chat.valuefetch;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.egov.chat.xternal.MdmsValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@Component
public class ValueFetcher {

    @Autowired
    List<ExternalValueFetcher> externalValueFetchers;

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

        String externalValueFetcherClassName = config.get("values").get("class").asText();
        ExternalValueFetcher externalValueFetcher = getExternalValueFetcher(externalValueFetcherClassName);

        ObjectNode params = createParamsToFetchValues(config, chatNode);

        return externalValueFetcher.getValues(params);
    }

    ObjectNode createParamsToFetchValues(JsonNode config, JsonNode chatNode) {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        ObjectNode params = mapper.createObjectNode();

        ObjectNode paramConfigurations = (ObjectNode) config.get("values").get("params");
        Iterator<String> paramKeys = paramConfigurations.fieldNames();

        while (paramKeys.hasNext()) {
            String key = paramKeys.next();
            String paramValue = "";

            String paramConfiguration = paramConfigurations.get(key).asText();

            if(paramConfiguration.substring(0, 1).equalsIgnoreCase("^")) {
                if(paramConfiguration.contains("tenantId")) {
                    try {
                        paramValue = chatNode.get("tenantId").asText();
                    } catch (Exception e) {
                        paramValue = "pb";
                    }
                }
            } else {
                paramValue = paramConfiguration;
            }

            params.set(key, TextNode.valueOf(paramValue));
        }

        return params;
    }

    ExternalValueFetcher getExternalValueFetcher(String className) {
        for(ExternalValueFetcher externalValueFetcher : externalValueFetchers) {
            if(externalValueFetcher.getClass().getName().equalsIgnoreCase(className))
                return externalValueFetcher;
        }
        return null;
    }

}
