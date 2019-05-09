package org.egov.chat.service.restendpoint;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.models.Message;
import org.egov.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Slf4j
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
        String conversationId = chatNode.at(JsonPointerNameConstants.conversationId).asText();

        log.info(conversationId);

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        ObjectNode params = mapper.createObjectNode();

        List<Message> messageList = messageRepository.getMessagesOfConversation(conversationId);

        ArrayNode paramConfig = (ArrayNode) config.get("nodes");

        for(JsonNode param : paramConfig) {
            String nodeId = param.asText();
            Optional<Message> message =
                    messageList.stream().filter(message1 -> message1.getNodeId().equalsIgnoreCase(nodeId)).findFirst();
            if(message.isPresent())
                params.set(nodeId, TextNode.valueOf(message.get().getMessageContent()));
            else
                params.set(nodeId, NullNode.getInstance());
        }

        log.info("Params of nodes : " + params.toString());

        ObjectNode paramConfigurations = (ObjectNode) config.get("params");
        Iterator<String> paramKeys = paramConfigurations.fieldNames();

        while (paramKeys.hasNext()) {
            String key = paramKeys.next();
            String paramValue = "";

            String paramConfiguration = paramConfigurations.get(key).asText();

            if(paramConfiguration.substring(0, 1).equalsIgnoreCase("/")) {
                paramValue = chatNode.at(paramConfiguration).asText();
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