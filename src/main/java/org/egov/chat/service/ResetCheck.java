package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.repository.ConversationStateRepository;
import org.egov.chat.util.KafkaStreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Service
public class ResetCheck {

    private String streamName = "reset-check";

    private List<String> resetKeywords = Arrays.asList("reset", "cancel");
    private int fuzzymatchScoreThreshold = 90;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ConversationStateRepository conversationStateRepository;

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

    public void startStream(String inputTopic, String outputTopic, String resetTopic) {

        Properties streamConfiguration = (Properties) defaultStreamConfiguration.clone();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, streamName);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> messagesKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(), jsonSerde));

        KStream<String, JsonNode>[] branches = messagesKStream.branch(
                (key, chatNode) -> ! isResetKeyword(chatNode),
                (key, value) -> true
        );

        branches[0].mapValues(chatNode -> chatNode).to(outputTopic, Produced.with(Serdes.String(), jsonSerde));

        branches[1].mapValues(chatNode -> {
            String conversationId = chatNode.at(JsonPointerNameConstants.conversationId).asText();

            conversationStateRepository.markConversationInactive(conversationId);

            return chatNode;
        }).to(resetTopic, Produced.with(Serdes.String(), jsonSerde));

        KafkaStreamUtil.startStream(builder, streamConfiguration);
    }

    private boolean isResetKeyword(JsonNode chatNode) {

        String answer = chatNode.at(JsonPointerNameConstants.messageContent).asText();

        for(String resetKeyword : resetKeywords) {
            int score = FuzzySearch.tokenSetRatio(resetKeyword, answer);
            if(score >= fuzzymatchScoreThreshold)
                return true;
        }

        return false;
    }

}
