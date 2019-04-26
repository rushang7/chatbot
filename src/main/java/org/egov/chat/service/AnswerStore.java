package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
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
        String node_id = config.get("name").asText();
        String conversation_id = chatNode.get("conversationId").asText();
        String messageContent = chatNode.get("answer").asText();

        Message message = Message.builder().messageId(UUID.randomUUID().toString())
                .conversationId(conversation_id).nodeId(node_id).messageContent(messageContent).build();
        messageRepository.insertMessage(message);

    }

}
