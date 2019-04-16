package org.egov.chat.answer;

import com.fasterxml.jackson.databind.JsonNode;
import org.egov.chat.models.Message;
import org.egov.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AnswerStore {

    @Autowired
    private MessageRepository messageRepository;

    public void saveAnswer(JsonNode config, JsonNode chatNode) {
        String node_id = config.get("name").asText();
        String conversation_id = chatNode.get("conversationId").asText();
        String messageContent = chatNode.get("answer").asText();

        Message message = Message.builder().message_id(UUID.randomUUID().toString())
                .conversation_id(conversation_id).node_id(node_id).message_content(messageContent).build();
        messageRepository.insertMessage(message);

    }

}
