package org.egov.chat.service.streams;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.egov.chat.service.AnswerExtractor;
import org.egov.chat.service.AnswerStore;
import org.egov.chat.service.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Slf4j
public class CreateStepStream extends CreateStream {

    @Autowired
    private Validator validator;
    @Autowired
    private AnswerExtractor answerExtractor;
    @Autowired
    private AnswerStore answerStore;


    public void createEvaluateAnswerStreamForConfig(JsonNode config, String answerInputTopic, String answerOutputTopic, String questionTopic) {

        String streamName = config.get("name").asText() + "-answer";

        Properties streamConfiguration = (Properties) defaultStreamConfiguration.clone();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, streamName);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> answerKStream = builder.stream(answerInputTopic, Consumed.with(Serdes.String(), jsonSerde));

        KStream<String, JsonNode>[] branches = answerKStream.branch(
                (key, chatNode) -> validator.isValid(config, chatNode),
                (key, value) -> true
        );

        branches[0].mapValues(chatNode -> {

            chatNode = answerExtractor.extractAnswer(config, chatNode);

            answerStore.saveAnswer(config, chatNode);

            return chatNode;
        }).to(answerOutputTopic, Produced.with(Serdes.String(), jsonSerde));

        branches[1].mapValues(value -> value).to(questionTopic, Produced.with(Serdes.String(), jsonSerde));

        startStream(builder, streamConfiguration);

        log.info("Stream started : " + streamName + ", from : " + answerInputTopic + ", to : " + answerOutputTopic +
                " OR to : " + questionTopic);
    }


}
