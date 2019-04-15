package org.egov.chat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.chat.segregation.InputSegregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private InputSegregator inputSegregator;

    @Autowired
    public ChatController(InputSegregator inputSegregator) {
        this.inputSegregator = inputSegregator;
    }

    @KafkaListener(groupId = "input-segregator", topics = "input-answer")
    public void segregateInput(ConsumerRecord<String, JsonNode> consumerRecord) {
        inputSegregator.segregateAnswer(consumerRecord);
    }

}
