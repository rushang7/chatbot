package org.egov.chat.pre.controller;

import org.egov.chat.pre.enrichment.UserDataEnricher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

@Controller
public class EnrichController {

    @Autowired
    private UserDataEnricher userDataEnricher;

    @PostConstruct
    public void initPreChatbotStreams() {
        userDataEnricher.startUserDataStream("tenant-enriched-messages", "input-messages");
    }

}
