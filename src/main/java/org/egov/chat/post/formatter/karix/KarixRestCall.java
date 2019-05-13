package org.egov.chat.post.formatter.karix;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class KarixRestCall {

    String karixEndpoint = "https://rcmapisandbox.instaalerts.zone/services/rcm/sendMessage";
    String karixAuthorizationKey = "Bearer s6iyrz5y8rPApBQ2gQ3oog==";

    @Autowired
    private RestTemplate restTemplate;



    public void sendMessage(JsonNode response) {



        HttpHeaders httpHeaders = getDefaultHttpHeaders();

        HttpEntity<JsonNode> request = new HttpEntity<>(response, httpHeaders);

        ResponseEntity<JsonNode> karixResponse = restTemplate.postForEntity(karixEndpoint, request, JsonNode.class);

        log.info("Karix Send Message Response : " + karixResponse.toString());

    }

    HttpHeaders getDefaultHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authentication", karixAuthorizationKey);
        return headers;
    }


}
