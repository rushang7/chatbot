package org.egov.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.egov.chat.streams.CreateStepStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.io.IOException;

@ComponentScan(basePackages = { "org.egov.chat" , "org.egov.chat.config"})
@SpringBootApplication
public class ChatBot {

    @Autowired
    private CreateStepStream createStepStream;

    public static void main(String args[]) {

        SpringApplication.run(ChatBot.class, args);

    }

    @PostConstruct
    public void run() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        JsonNode config = mapper.readTree(ChatBot.class.getClassLoader().getResource("graph/pgr/create/pgr.create.address.yaml"));

        createStepStream.createEvaluateAnswerStreamForConfig(config, "address-answer-input", "address-answer-output", "address-question");

        createStepStream.createQuestionStreamForConfig(config, "address-question", "send-message");

    }

}
