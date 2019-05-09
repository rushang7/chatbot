package org.egov.chat.pre.enrichment;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.egov.chat.config.ApplicationProperties;
import org.egov.chat.pre.authorization.UserService;
import org.egov.chat.util.KafkaStreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Service
public class UserDataEnricher {

    private String streamName = "user-data-enrich";

    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private UserService userService;

    private Properties defaultStreamConfiguration;
    private Serde<JsonNode> jsonSerde;

    @PostConstruct
    public void init() {
        this.defaultStreamConfiguration = new Properties();
        defaultStreamConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperties.getKafkaHost());

        Serializer<JsonNode> jsonSerializer = new JsonSerializer();
        Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
        jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    }


    public void startUserDataStream(String inputTopic, String outputTopic) {

        Properties streamConfiguration = (Properties) defaultStreamConfiguration.clone();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, streamName);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> messagesKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(), jsonSerde));

        messagesKStream.mapValues(chatNode -> {
            userService.addLoggedInUser(chatNode);
            return chatNode;
        }).to(outputTopic, Produced.with(Serdes.String(), jsonSerde));

        KafkaStreamUtil.startStream(builder, streamConfiguration);
    }


}
