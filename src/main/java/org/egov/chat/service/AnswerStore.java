package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.models.Message;
import org.egov.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AnswerStore {

    @Autowired
    private MessageRepository messageRepository;

    public void saveAnswer(JsonNode config, JsonNode chatNode) {

        String nodeId = config.get("name").asText();
        String conversationId = chatNode.at(JsonPointerNameConstants.conversationId).asText();
        String messageContent = chatNode.at(JsonPointerNameConstants.messageContent).asText();

        Message message = Message.builder().messageId(UUID.randomUUID().toString())
                .conversationId(conversationId).nodeId(nodeId).messageContent(messageContent).build();
        messageRepository.insertMessage(message);

    }

}
