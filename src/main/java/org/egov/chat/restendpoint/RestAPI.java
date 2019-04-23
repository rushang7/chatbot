package org.egov.chat.restendpoint;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.egov.chat.models.Message;
import org.egov.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class RestAPI {

    @Autowired
    private List<RestEndpoint> restEndpointList;

    @Autowired
    private MessageRepository messageRepository;


    public String makeRestEndpointCall(JsonNode config, JsonNode chatNode) {

        String restClassName = config.get("class").asText();

        RestEndpoint restEndpoint = getRestEndpointClass(restClassName);

        ObjectNode params = makeParamsforConfig(config, chatNode);

        return restEndpoint.messageForRestCall(params);
    }

    private ObjectNode makeParamsforConfig(JsonNode config, JsonNode chatNode) {
        String conversationId = chatNode.get("conversationId").asText();

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        ObjectNode params = mapper.createObjectNode();

        ArrayNode paramConfig = (ArrayNode) config.get("nodes");

        for(JsonNode param : paramConfig) {
            String nodeId = param.asText();
            Message message = messageRepository.getMessageForConversationAndNode(conversationId, nodeId);
            params.set(nodeId, TextNode.valueOf(message.getMessageContent()));
        }
        ObjectNode paramConfigurations = (ObjectNode) config.get("params");
        Iterator<String> paramKeys = paramConfigurations.fieldNames();

        while (paramKeys.hasNext()) {
            String key = paramKeys.next();
            String paramValue = "";

            String paramConfiguration = paramConfigurations.get(key).asText();

            if(paramConfiguration.substring(0, 1).equalsIgnoreCase("^")) {
                if(paramConfiguration.contains("tenantId")) {
                    paramValue = chatNode.get("tenantId").asText();
                } else if(paramConfiguration.contains("mobileNumber")) {
                    paramValue = chatNode.get("user").get("mobileNumber").asText();
                }
            } else {
                paramValue = paramConfiguration;
            }

            params.set(key, TextNode.valueOf(paramValue));
        }


        return params;
    }

    RestEndpoint getRestEndpointClass(String className) {
        for(RestEndpoint restEndpoint : restEndpointList) {
            if(restEndpoint.getClass().getName().equalsIgnoreCase(className))
                return restEndpoint;
        }
        return null;
    }

}
