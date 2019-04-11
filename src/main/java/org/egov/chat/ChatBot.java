package org.egov.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.kafka.streams.StreamsConfig;
import org.egov.chat.streams.CreateStepStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

@ComponentScan(basePackages = { "org.egov.chat" , "org.egov.chat.config"})
@SpringBootApplication(exclude = {EmbeddedServletContainerAutoConfiguration.class, WebMvcAutoConfiguration.class})
public class ChatBot {

    @Autowired
    private static CreateStepStream createStepStream;

    public static void main(String args[]) throws Exception {

        new SpringApplication().setWebEnvironment(false);

        SpringApplication.run(ChatBot.class, args);

    }

    @PostConstruct
    public void run() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        JsonNode config = mapper.readTree(ChatBot.class.getClassLoader().getResource("graph/pgr/create/pgr.create.address.yaml"));

        createStepStream.createQuestionStreamForConfig(config, "address-question", "send-message");


    }

}
