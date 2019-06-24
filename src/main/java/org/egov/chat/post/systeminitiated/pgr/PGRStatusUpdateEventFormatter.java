package org.egov.chat.post.systeminitiated.pgr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.egov.chat.config.KafkaStreamsConfig;
import org.egov.chat.post.systeminitiated.SystemInitiatedEventFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
public class PGRStatusUpdateEventFormatter implements SystemInitiatedEventFormatter {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private KafkaStreamsConfig kafkaStreamsConfig;


    @Value("${state.level.tenant.id}")
    private String stateLevelTenantId;
    @Value("${egov.external.host}")
    private String egovExternalHost;

    @Value("${user.service.host}")
    private String userServiceHost;
    @Value("${user.service.search.path}")
    private String userServiceSearchPath;

    private String userServiceSearchRequest = "{\"RequestInfo\":{},\"tenantId\":\"\",\"id\":[\"\"]}";

    @Override
    public String getStreamName() {
        return "pgr-update-formatter";
    }

    @Override
    public void startStream(String inputTopic, String outputTopic) {
        Properties streamConfiguration = kafkaStreamsConfig.getDefaultStreamConfiguration();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, getStreamName());
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> messagesKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(),
                kafkaStreamsConfig.getJsonSerde()));

        messagesKStream.flatMapValues(event -> {
            try {
                return createChatNodes(event);
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        }).to(outputTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        kafkaStreamsConfig.startStream(builder, streamConfiguration);

    }

    @Override
    public List<JsonNode> createChatNodes(JsonNode event) throws Exception {
        String tenantId = stateLevelTenantId;
        String mobileNumber = event.at("/services/0/citizen/mobileNumber").asText();

        ObjectNode chatNode = objectMapper.createObjectNode();
        chatNode.put("tenantId", tenantId);

        ObjectNode user = objectMapper.createObjectNode();
        user.put("mobileNumber", mobileNumber);
        chatNode.set("user", user);

        chatNode.set("response", createResponseMessage(event));

        return Collections.singletonList(chatNode);
    }

    private JsonNode createResponseMessage(JsonNode event) throws IOException {
        String status = event.at("/services/0/status").asText();

        if(status.equalsIgnoreCase("resolved")) {
            return responseForResolvedStatus(event);
        } else if(status.equalsIgnoreCase("assigned")) {
            return responseForAssignedtatus(event);
        }

        return null;
    }

    private JsonNode responseForAssignedtatus(JsonNode event) throws IOException {
        String serviceRequestId = event.at("/services/0/serviceRequestId").asText();
        String serviceCode = event.at("/services/0/serviceCode").asText();

        JsonNode assignee = getAssignee(event);
        String assigneeMobileNumber = assignee.at("/mobileNumber").asText();

        String message = "Your complaint has been assigned.";
        message += "\nComplaint Number : " + serviceRequestId;
        message += "\nCategory : " + serviceCode;
        message += "\nAssignee Mobile Number : " + assigneeMobileNumber;

        ObjectNode responseMessage = objectMapper.createObjectNode();

        responseMessage.put("type", "text");
        responseMessage.put("text", message);

        return responseMessage;
    }

    private JsonNode getAssignee(JsonNode event) throws IOException {

        String assigneeId = event.at("/actionInfo/0/assignee").asText();
        String tenantId = event.at("/actionInfo/0/tenantId").asText();

        DocumentContext request = JsonPath.parse(userServiceSearchRequest);

        request.set("$.tenantId", tenantId);
        request.set("$.id.[0]", assigneeId);

        JsonNode requestObject = null;
        requestObject = objectMapper.readTree(request.jsonString());

        ResponseEntity<ObjectNode> response = restTemplate.postForEntity(userServiceHost + userServiceSearchPath,
                    requestObject, ObjectNode.class);

        return response.getBody().at("/user/0");
    }

    private JsonNode responseForResolvedStatus(JsonNode event) throws UnsupportedEncodingException {

        String serviceRequestId = event.at("/services/0/serviceRequestId").asText();
        String serviceCode = event.at("/services/0/serviceCode").asText();

        String message = "Your complaint has been assigned.";
        message += "\nComplaint Number : " + serviceRequestId;
        message += "\nCategory : " + serviceCode;
        message += "\nYou can rate the service here : " + makeURLForComplaint(serviceRequestId);

        ObjectNode responseMessage = objectMapper.createObjectNode();

        responseMessage.put("type", "text");
        responseMessage.put("text", message);

        return responseMessage;
    }

    private String makeURLForComplaint(String serviceRequestId) throws UnsupportedEncodingException {
        String encodedPath = URLEncoder.encode( serviceRequestId, "UTF-8" );
        String url = egovExternalHost + "/citizen/complaint-details/" + encodedPath;
        return url;
    }

}
