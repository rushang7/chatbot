package org.egov.chat.pre.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.egov.chat.config.ApplicationProperties;
import org.egov.chat.config.KafkaStreamsConfig;
import org.egov.chat.pre.authorization.UserService;
import org.egov.chat.pre.config.JsonPointerNameConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;

@Service
public class TenantIdEnricher {

    private String streamName = "tenant-enrich";

    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private KafkaStreamsConfig kafkaStreamsConfig;
    @Autowired
    private UserService userService;


    private Map<String, String> numberToTenantId;

    // TODO : mobileNumber to tenantId mapping
    public String getTenantIdFor(String recipientNumber) {
        return "pb.amritsar";
//        return numberToTenantId.get(recipientNumber);
    }

    public void startTenantEnricherStream(String inputTopic, String outputTopic) {

        Properties streamConfiguration = kafkaStreamsConfig.getDefaultStreamConfiguration();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, streamName);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> messagesKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(),
                kafkaStreamsConfig.getJsonSerde()));

        messagesKStream.mapValues(chatNode -> {
            String recipientNumber = chatNode.at(JsonPointerNameConstants.recipientNumber).asText();
            ( (ObjectNode) chatNode).put("tenantId", getTenantIdFor(recipientNumber));
            return chatNode;
        }).to(outputTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        kafkaStreamsConfig.startStream(builder, streamConfiguration);
    }

}
