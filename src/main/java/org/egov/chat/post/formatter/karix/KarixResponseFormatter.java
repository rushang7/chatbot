package org.egov.chat.post.formatter.karix;

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
import org.egov.chat.post.formatter.ResponseFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Slf4j
@Component
public class KarixResponseFormatter implements ResponseFormatter {

    String karixRequestBody = "{\"message\":{\"channel\":\"WABA\",\"content\":{\"preview_url\":false,\"type\":\"TEXT\",\"text\":\"\"},\"recipient\":{\"to\":\"\",\"recipient_type\":\"individual\",\"reference\":{\"cust_ref\":\"Some Customer Ref\",\"messageTag1\":\"Message Tag Val1\",\"conversationId\":\"Some Optional Conversation ID\"}},\"sender\":{\"from\":\"919845315868\"},\"preferences\":{\"webHookDNId\":\"sandbox\"}},\"metaData\":{\"version\":\"v1.0.9\"}}";

    @Autowired
    private KafkaStreamsConfig kafkaStreamsConfig;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getStreamName() {
        return "karix-response-transform";
    }

    @Override
    public JsonNode getTransformedResponse(JsonNode response) {
        DocumentContext request = JsonPath.parse(karixRequestBody);

        request.set("$.message.content.type", response.at(KarixJsonPointerConstants.responseType).asText());
        request.set("$.message.content.text", response.at(KarixJsonPointerConstants.responseText).asText());
        request.set("$.message.recipient.to", "91" + response.at(KarixJsonPointerConstants.toMobileNumber).asText());
        request.set("$.message.sender.from", response.at(KarixJsonPointerConstants.fromMobileNumber).asText());

        JsonNode karixRequest = null;
        try {
            karixRequest = objectMapper.readTree(request.jsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return karixRequest;
    }

    @Override
    public void startResponseStream(String inputTopic, String outputTopic) {
        Properties streamConfiguration = kafkaStreamsConfig.getDefaultStreamConfiguration();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, getStreamName());
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> messagesKStream = builder.stream(inputTopic, Consumed.with(Serdes.String(),
                kafkaStreamsConfig.getJsonSerde()));

        messagesKStream.mapValues(response -> {
            try {
                return getTransformedResponse(response);
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        }).to(outputTopic, Produced.with(Serdes.String(), kafkaStreamsConfig.getJsonSerde()));

        kafkaStreamsConfig.startStream(builder, streamConfiguration);

    }
}
