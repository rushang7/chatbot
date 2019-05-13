package org.egov.chat.post.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.chat.post.formatter.karix.KarixResponseFormatter;
import org.egov.chat.post.formatter.karix.KarixRestCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

@Controller
public class PostChatController {

    @Autowired
    private KarixResponseFormatter karixResponseFormatter;
    @Autowired
    private KarixRestCall karixRestCall;


    @PostConstruct
    public void init() {
        karixResponseFormatter.startResponseStream("send-message", "karix-send-message");
    }

    @KafkaListener(groupId = "karix-rest-call", topics = "karix-send-message")
    public void sendMessage(ConsumerRecord<String, JsonNode> consumerRecord) {
        karixRestCall.sendMessage(consumerRecord.value());
    }


}
