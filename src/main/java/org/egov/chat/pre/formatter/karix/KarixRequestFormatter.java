package org.egov.chat.pre.formatter.karix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.egov.chat.config.KafkaStreamsConfig;
import org.egov.chat.pre.formatter.RequestFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
public class KarixRequestFormatter implements RequestFormatter {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaStreamsConfig kafkaStreamsConfig;

    @Override
    public String getStreamName() {
        return "karix-request-transform";
    }

    @Override
    public boolean isValid(JsonNode inputRequest) {

        try {
            log.debug("Karix Request Content Type : " + inputRequest.at(KairxJsonPointerConstants.contentType).asText());
            if(inputRequest.at(KairxJsonPointerConstants.contentType).asText().equalsIgnoreCase("text")) {

                return true;
            }

        } catch (Exception e) {

        }

        return false;
    }

    @Override
    public JsonNode getTransformedRequest(JsonNode inputRequest) {
        String inputMobile = inputRequest.at(KairxJsonPointerConstants.userMobileNumber).asText();
        String mobileNumber = inputMobile.substring(2, 2 + 10);
        ObjectNode user = objectMapper.createObjectNode();
        user.set("mobileNumber", TextNode.valueOf(mobileNumber));

        ObjectNode message = objectMapper.createObjectNode();
        message.set("type", inputRequest.at(KairxJsonPointerConstants.contentType));
        if(message.get("type").asText().equalsIgnoreCase("text")) {
            message.set("content", inputRequest.at(KairxJsonPointerConstants.textContent));
        }

        ObjectNode recipient = objectMapper.createObjectNode();
        recipient.set("to", inputRequest.at(KairxJsonPointerConstants.recipientMobileNumber));

        ObjectNode chatNode = objectMapper.createObjectNode();

        chatNode.set("user", user);
        chatNode.set("message", message);
        chatNode.set("recipient", recipient);

        return chatNode;
    }

    @Override
    public void startRequestFormatterStream(String inputTopic, String outputTopic, String errorTopic) {
        Properties streamConfiguration = kafkaStreamsConfig.getDefaultStreamConfiguration();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, getStreamName());
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> messagesKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(),
                kafkaStreamsConfig.getJsonSerde()));

        KStream<String, JsonNode>[] branches = messagesKStream.branch(
                (key, inputRequest) -> isValid(inputRequest),
                (key, value) -> true
        );

        branches[0].mapValues(request -> {
            try {
                return getTransformedRequest(request);
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        }).to(outputTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        branches[1].mapValues(request -> request).to(errorTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        kafkaStreamsConfig.startStream(builder, streamConfiguration);
    }

}
