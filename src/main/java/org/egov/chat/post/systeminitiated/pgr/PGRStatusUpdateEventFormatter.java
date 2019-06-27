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
import org.egov.chat.xternal.util.LocalizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
    @Autowired
    private LocalizationService localizationService;

    private String complaintCategoryLocalizationPrefix = "pgr.complaint.category.";

    @Value("${state.level.tenant.id}")
    private String stateLevelTenantId;
    @Value("${egov.external.host}")
    private String egovExternalHost;

    @Value("${user.service.host}")
    private String userServiceHost;
    @Value("${user.service.search.path}")
    private String userServiceSearchPath;

    private String userServiceSearchRequest = "{\"RequestInfo\":{},\"tenantId\":\"\",\"id\":[\"\"]}";

    private String shareMessage = "Please share this with your friends and family : https://api.whatsapp.com/send?phone=919845315868&text=Hi";

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
                return Collections.emptyList();
            }
        }).to(outputTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        kafkaStreamsConfig.startStream(builder, streamConfiguration);

    }

    @Override
    public List<JsonNode> createChatNodes(JsonNode event) throws Exception {
        List<JsonNode> chatNodes = new ArrayList<>();

        String mobileNumber = event.at("/services/0/citizen/mobileNumber").asText();

        ObjectNode chatNode = objectMapper.createObjectNode();
        chatNode.put("tenantId", stateLevelTenantId);

        ObjectNode user = objectMapper.createObjectNode();
        user.put("mobileNumber", mobileNumber);
        chatNode.set("user", user);

        chatNode.set("response", createResponseMessage(event));

        chatNodes.add(chatNode);

        String status = event.at("/services/0/status").asText();

        if(status.equalsIgnoreCase("assigned")) {
            chatNodes.addAll(createChatNodeForAssignee(event));
        } else if(status.equalsIgnoreCase("resolved")) {
            chatNodes.add(createShareNode(chatNode));
        }

        return chatNodes;
    }

    private JsonNode createShareNode(ObjectNode chatNode) {
        ObjectNode shareNode = chatNode.deepCopy();

        ((ObjectNode) shareNode.get("response")).put("text", shareMessage);

        return shareNode;
    }

    private List<JsonNode> createChatNodeForAssignee(JsonNode event) throws IOException {
        List<JsonNode> chatNodes = new ArrayList<>();

        JsonNode assignee = getAssignee(event);
        String assigneeMobileNumber = assignee.at("/mobileNumber").asText();

        ObjectNode chatNode = objectMapper.createObjectNode();
        chatNode.put("tenantId", stateLevelTenantId);

        ObjectNode user = objectMapper.createObjectNode();
        user.put("mobileNumber", assigneeMobileNumber);
        chatNode.set("user", user);

        chatNode.set("response", createResponseMessageForAssignee(event, assignee));

        chatNodes.add(chatNode);

        if(eventContainsLocation(event))
            chatNodes.add(createLocationNode(event, assignee));

        return chatNodes;
    }

    private boolean eventContainsLocation(JsonNode event) {
        if(event.get("services").get(0).get("addressDetail").get("latitude") != null)
            return true;
        return false;
    }

    private JsonNode createLocationNode(JsonNode event, JsonNode assignee) {
        ObjectNode chatNode = objectMapper.createObjectNode();
        chatNode.put("tenantId", stateLevelTenantId);

        ObjectNode user = objectMapper.createObjectNode();
        user.put("mobileNumber", assignee.at("/mobileNumber").asText());
        chatNode.set("user", user);

        chatNode.set("response", createLocationResponse(event));

        return chatNode;
    }

    private JsonNode createLocationResponse(JsonNode event) {
        ObjectNode responseMessage = objectMapper.createObjectNode();
        responseMessage.put("type", "location");
        ObjectNode location = objectMapper.createObjectNode();
        location.put("latitude", event.at("/services/0/addressDetail/latitude").toString());
        location.put("longitude", event.at("/services/0/addressDetail/longitude").toString());
        responseMessage.set("location", location);
        return responseMessage;
    }

    private JsonNode createResponseMessageForAssignee(JsonNode event, JsonNode assignee) throws UnsupportedEncodingException {
        ObjectNode responseMessage = objectMapper.createObjectNode();
        responseMessage.put("type", "text");
        String serviceRequestId = event.at("/services/0/serviceRequestId").asText();
        String serviceCode = event.at("/services/0/serviceCode").asText();

        String message = "";
        message += "Hey " + assignee.at("/name").asText() + ",";
        message += "\nYou have been assigned a new complaint to resolve.";
        message += "\nComplaint Number : " + serviceRequestId;
        message += "\nCategory : " + localizationService.getMessageForCode(complaintCategoryLocalizationPrefix + serviceCode);
        message += "\n" + makeEmployeeURLForComplaint(serviceRequestId);

        responseMessage.put("text", message);

        return responseMessage;
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
        message += "\nCategory : " + localizationService.getMessageForCode(complaintCategoryLocalizationPrefix + serviceCode);
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

        String message = "Your complaint has been resolved.";
        message += "\nComplaint Number : " + serviceRequestId;
        message += "\nCategory : " + localizationService.getMessageForCode(complaintCategoryLocalizationPrefix + serviceCode);
        message += "\nYou can rate the service here : " + makeCitizenURLForComplaint(serviceRequestId);

        ObjectNode responseMessage = objectMapper.createObjectNode();

        responseMessage.put("type", "text");
        responseMessage.put("text", message);

        return responseMessage;
    }

    private String makeCitizenURLForComplaint(String serviceRequestId) throws UnsupportedEncodingException {
        String encodedPath = URLEncoder.encode( serviceRequestId, "UTF-8" );
        String url = egovExternalHost + "/citizen/complaint-details/" + encodedPath;
        return url;
    }

    private String makeEmployeeURLForComplaint(String serviceRequestId) throws UnsupportedEncodingException {
        String encodedPath = URLEncoder.encode( serviceRequestId, "UTF-8" );
        String url = egovExternalHost + "/employee/complaint-details/" + encodedPath;
        return url;
    }

}
