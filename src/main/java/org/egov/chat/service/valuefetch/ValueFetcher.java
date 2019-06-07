package org.egov.chat.service.valuefetch;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.models.Message;
import org.egov.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class ValueFetcher {

    @Autowired
    List<ExternalValueFetcher> externalValueFetchers;

    @Autowired
    private MessageRepository messageRepository;

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

    public String getCodeForValue(JsonNode config, JsonNode chatNode, String answer) {
        if(config.get("values").isArray()) {
            return answer;
        } else {
            String externalValueFetcherClassName = config.get("values").get("class").asText();
            ExternalValueFetcher externalValueFetcher = getExternalValueFetcher(externalValueFetcherClassName);

            ObjectNode params = createParamsToFetchValues(config, chatNode);

            return externalValueFetcher.getCodeForValue(params, answer);
        }
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
            JsonNode paramValue;

            String paramConfiguration = paramConfigurations.get(key).asText();

            if(paramConfiguration.substring(0, 1).equalsIgnoreCase("/")) {
                paramValue = chatNode.at(paramConfiguration);
            } else if(paramConfiguration.substring(0, 1).equalsIgnoreCase("~")) {
                String nodeId = paramConfiguration.substring(1);
                String conversationId = chatNode.at(JsonPointerNameConstants.conversationId).asText();
                List<Message> messages = messageRepository.getMessagesOfConversation(conversationId);
                paramValue = TextNode.valueOf(findMessageForNode(messages, nodeId));
            } else {
                paramValue = TextNode.valueOf(paramConfiguration);
            }

            params.set(key, paramValue);
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

    String findMessageForNode(List<Message> messages, String nodeId) {
        for(Message message : messages) {
            if(message.getNodeId().equalsIgnoreCase(nodeId)){
                return message.getMessageContent();
            }
        }
        return null;
    }

}
