package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.egov.chat.config.ApplicationProperties;
import org.egov.chat.models.ConversationState;
import org.egov.chat.repository.ConversationStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.UUID;

@Service
public class InitiateConversation {

    private String streamName = "initiate-conversation";

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ConversationStateRepository conversationStateRepository;
    @Autowired
    private KafkaTemplate<String, JsonNode> kafkaTemplate;

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

    public void startStream(String inputTopic, String outputTopic) {

        Properties streamConfiguration = (Properties) defaultStreamConfiguration.clone();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, streamName);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> messagesKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(), jsonSerde));

        messagesKStream.mapValues(chatNode -> createOrContinueConversation(chatNode)).to(outputTopic, Produced.with(Serdes.String(), jsonSerde));

        startStream(builder, streamConfiguration);

    }

    public JsonNode createOrContinueConversation(JsonNode chatNode) {

        String userId = chatNode.get("user").get("userId").asText();

        ConversationState conversationState = conversationStateRepository.getConversationStateForUserId(userId);

        if(conversationState == null) {
            conversationState = createNewConversationForUser(userId);
            conversationStateRepository.insertNewConversation(conversationState);
        }

        chatNode = ((ObjectNode) chatNode).set("conversationId",
                TextNode.valueOf(conversationState.getConversationId()));

        return chatNode;
    }

    private ConversationState createNewConversationForUser(String userId) {
        String conversationId = UUID.randomUUID().toString();
        return ConversationState.builder().conversationId(conversationId).userId(userId).active(true).build();
    }

    private void startStream(StreamsBuilder builder, Properties streamConfiguration) {
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamConfiguration);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }


}
