package org.egov.chat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.chat.service.InitiateConversation;
import org.egov.chat.service.InputSegregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private InputSegregator inputSegregator;
    @Autowired
    private InitiateConversation initiateConversation;


    @KafkaListener(groupId = "initiate-conversation", topics = "input-messages")
    public void initiateConversation(ConsumerRecord<String, JsonNode> consumerRecord) {
        initiateConversation.createOrContinueConversation(consumerRecord);
    }

    @KafkaListener(groupId = "input-segregator", topics = "input-answer")
    public void segregateInput(ConsumerRecord<String, JsonNode> consumerRecord) {
        inputSegregator.segregateAnswer(consumerRecord);
    }

}
