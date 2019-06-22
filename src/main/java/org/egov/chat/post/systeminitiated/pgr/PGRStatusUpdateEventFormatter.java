package org.egov.chat.post.systeminitiated.pgr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

@Slf4j
@Component
public class PGRStatusUpdateEventFormatter implements SystemInitiatedEventFormatter {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaStreamsConfig kafkaStreamsConfig;


    @Value("${state.level.tenant.id}")
    private String stateLevelTenantId;
    @Value("${egov.external.host}")
    private String egovExternalHost;

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

        messagesKStream.mapValues(event -> {
            try {
                return createChatNode(event);
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        }).to(outputTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        kafkaStreamsConfig.startStream(builder, streamConfiguration);

    }

    @Override
    public JsonNode createChatNode(JsonNode event) throws Exception {
        String tenantId = stateLevelTenantId;
        String mobileNumber = event.at("/services/0/citizen/mobileNumber").asText();

        ObjectNode chatNode = objectMapper.createObjectNode();
        chatNode.put("tenantId", tenantId);

        ObjectNode user = objectMapper.createObjectNode();
        user.put("mobileNumber", mobileNumber);
        chatNode.set("user", user);

        chatNode.set("response", createResponseMessage(event));

        return chatNode;
    }

    private JsonNode createResponseMessage(JsonNode event) throws UnsupportedEncodingException {
        String status = event.at("/services/0/status").asText();

        if(status.equalsIgnoreCase("resolved")) {
            return responseForResolvedStatus(event);
        } else if(status.equalsIgnoreCase("assigned")) {
            return responseForAssignedtatus(event);
        }

        return null;
    }

    private JsonNode responseForAssignedtatus(JsonNode event) {
        String serviceRequestId = event.at("/services/0/serviceRequestId").asText();
        String serviceCode = event.at("/services/0/serviceCode").asText();

        String message = "Your complaint has been assigned.";
        message += "\nComplaint Number : " + serviceRequestId;
        message += "\nCategory : " + serviceCode;

        ObjectNode responseMessage = objectMapper.createObjectNode();

        responseMessage.put("type", "text");
        responseMessage.put("text", message);

        return responseMessage;
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
