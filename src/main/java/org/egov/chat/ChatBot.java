package org.egov.chat;

import org.egov.chat.controller.GraphStreamGenerator;
import org.egov.chat.controller.StreamController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.io.IOException;

@ComponentScan(basePackages = { "org.egov.chat" })
@SpringBootApplication
public class ChatBot {

    public static void main(String args[]) {
        SpringApplication.run(ChatBot.class, args);
    }

}
