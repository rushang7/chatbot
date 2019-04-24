package org.egov.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.egov.chat.config.graph.GraphReader;
import org.egov.chat.config.graph.TopicNameGetter;
import org.egov.chat.controller.GraphStreamGenerator;
import org.egov.chat.controller.StreamController;
import org.egov.chat.service.streams.CreateBranchStream;
import org.egov.chat.service.streams.CreateEndpointStream;
import org.egov.chat.service.streams.CreateStepStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

@ComponentScan(basePackages = { "org.egov.chat" , "org.egov.chat.config"})
@SpringBootApplication
public class ChatBot {

    @Autowired
    private GraphStreamGenerator graphStreamGenerator;
    @Autowired
    private StreamController streamController;


    public static void main(String args[]) {

        SpringApplication.run(ChatBot.class, args);

    }

    @PostConstruct
    public void run() throws IOException {
        streamController.generateStreams();
        graphStreamGenerator.generateGraphStreams();
    }

}
