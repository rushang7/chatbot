package org.egov.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.egov.chat.config.ApplicationProperties;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.repository.ConversationStateRepository;
import org.egov.chat.config.KafkaStreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Service
public class ResetCheck {

    private String streamName = "reset-check";

    private List<String> resetKeywords = Arrays.asList("reset", "cancel");
    private int fuzzymatchScoreThreshold = 90;

    @Autowired
    private KafkaStreamsConfig kafkaStreamsConfig;

    @Autowired
    private ConversationStateRepository conversationStateRepository;

    public void startStream(String inputTopic, String outputTopic, String resetTopic) {

        Properties streamConfiguration = kafkaStreamsConfig.getDefaultStreamConfiguration();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, streamName);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> messagesKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(),
                kafkaStreamsConfig.getJsonSerde()));

        KStream<String, JsonNode>[] branches = messagesKStream.branch(
                (key, chatNode) -> ! isResetKeyword(chatNode),
                (key, value) -> true
        );

        branches[0].mapValues(chatNode -> chatNode).to(outputTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        branches[1].mapValues(chatNode -> {
            String conversationId = chatNode.at(JsonPointerNameConstants.conversationId).asText();

            conversationStateRepository.markConversationInactive(conversationId);

            return chatNode;
        }).to(resetTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        kafkaStreamsConfig.startStream(builder, streamConfiguration);
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
