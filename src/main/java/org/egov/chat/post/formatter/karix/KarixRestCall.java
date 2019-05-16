package org.egov.chat.post.formatter.karix;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@PropertySource("classpath:application.properties")
@Slf4j
@Service
public class KarixRestCall {

    String karixEndpoint = "https://rcmapisandbox.instaalerts.zone/services/rcm/sendMessage";

    @Value("karix.authentication.key")
    String karixAuthorizationKey;

    @Autowired
    private RestTemplate restTemplate;

    public void sendMessage(JsonNode response) {
        try {
            HttpHeaders httpHeaders = getDefaultHttpHeaders();

            HttpEntity<JsonNode> request = new HttpEntity<>(response, httpHeaders);

            ResponseEntity<JsonNode> karixResponse = restTemplate.postForEntity(karixEndpoint, request, JsonNode.class);

            log.info("Karix Send Message Response : " + karixResponse.toString());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    HttpHeaders getDefaultHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authentication", karixAuthorizationKey);
        return headers;
    }


}
