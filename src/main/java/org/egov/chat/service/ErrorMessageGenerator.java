package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ErrorMessageGenerator {

    @Autowired
    private ObjectMapper objectMapper;

    public JsonNode getErrorMessageNode(JsonNode config, JsonNode chatNode) {
        String errorMessage = getErrorMessageForConfig(config);
        if(errorMessage == null) {
            return null;
        }

        JsonNode errorMessageNode = chatNode.deepCopy();

        List<String> localizationCodes = Collections.singletonList(getErrorMessageForConfig(config));
        ArrayNode localizationCodesArrayNode = objectMapper.valueToTree(localizationCodes);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "text");
        response.set("localizationCodes", localizationCodesArrayNode);

        ((ObjectNode) errorMessageNode).set("response", response);

        return errorMessageNode;
    }

    private String getErrorMessageForConfig(JsonNode config) {
        if(config.has("errorMessage"))
            return config.get("errorMessage").asText();
        return null;
    }

}
