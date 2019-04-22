package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.chat.models.ConversationState;
import org.egov.chat.repository.ConversationStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InitiateConversation {

    private String outputTopic = "input-answer";

    @Autowired
    private ConversationStateRepository conversationStateRepository;
    @Autowired
    private KafkaTemplate<String, JsonNode> kafkaTemplate;

    public void createOrContinueConversation(ConsumerRecord<String, JsonNode> consumerRecord) {
        JsonNode chatNode = consumerRecord.value();

        String userId = chatNode.get("user").get("userId").asText();

        ConversationState conversationState = conversationStateRepository.getConversationStateForUserId(userId);

        if(conversationState == null) {
            conversationState = createNewConversationForUser(userId);
            conversationStateRepository.insertNewConversation(conversationState);
        }

        chatNode = ((ObjectNode) chatNode).set("conversationId",
                TextNode.valueOf(conversationState.getConversationId()));

        kafkaTemplate.send(outputTopic, consumerRecord.key(), chatNode);
    }

    private ConversationState createNewConversationForUser(String userId) {
        String conversationId = UUID.randomUUID().toString();
        return ConversationState.builder().conversationId(conversationId).userId(userId).active(true).build();
    }


}
