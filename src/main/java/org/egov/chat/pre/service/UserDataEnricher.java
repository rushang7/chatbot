package org.egov.chat.pre.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.egov.chat.config.ApplicationProperties;
import org.egov.chat.pre.authorization.UserService;
import org.egov.chat.config.KafkaStreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class UserDataEnricher {

    private String streamName = "user-data-enrich";

    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private KafkaStreamsConfig kafkaStreamsConfig;
    @Autowired
    private UserService userService;


    public void startUserDataStream(String inputTopic, String outputTopic) {

        Properties streamConfiguration = kafkaStreamsConfig.getDefaultStreamConfiguration();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, streamName);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> messagesKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(),
                kafkaStreamsConfig.getJsonSerde()));

        messagesKStream.mapValues(chatNode -> {
            userService.addLoggedInUser(chatNode);
            return chatNode;
        }).to(outputTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        kafkaStreamsConfig.startStream(builder, streamConfiguration);
    }


}
