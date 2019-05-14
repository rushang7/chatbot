package org.egov.chat.service.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.egov.chat.config.JsonPointerNameConstants;
import org.egov.chat.config.KafkaStreamsConfig;
import org.egov.chat.repository.MessageRepository;
import org.egov.chat.service.restendpoint.RestAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
public class CreateEndpointStream extends CreateStream {

    @Autowired
    private KafkaStreamsConfig kafkaStreamsConfig;

    @Autowired
    private RestAPI restAPI;

    @Autowired
    private MessageRepository messageRepository;

    public void createEndpointStream(JsonNode config, String inputTopic, String sendMessageTopic) {

        String streamName = config.get("name").asText() + "-answer";

        Properties streamConfiguration = kafkaStreamsConfig.getDefaultStreamConfiguration();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, streamName);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> answerKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(),
                kafkaStreamsConfig.getJsonSerde()));

        answerKStream.mapValues(chatNode -> {
            try {
                String responseMessage = restAPI.makeRestEndpointCall(config, chatNode);

                chatNode = ((ObjectNode) chatNode).set("question", TextNode.valueOf(responseMessage));

                String conversationId = chatNode.at(JsonPointerNameConstants.conversationId).asText();
                conversationStateRepository.markConversationInactive(conversationId);

                return chatNode;
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        }).to(sendMessageTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        kafkaStreamsConfig.startStream(builder, streamConfiguration);

        log.info("Endpoint Stream started : " + streamName + ", from : " + inputTopic + ", to : " + sendMessageTopic);
    }

}
