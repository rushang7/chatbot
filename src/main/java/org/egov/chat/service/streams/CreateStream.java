package org.egov.chat.service.streams;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
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
import org.egov.chat.service.QuestionGenerator;
import org.egov.chat.repository.ConversationStateRepository;
import org.egov.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
@Slf4j
public class CreateStream {

    protected Properties defaultStreamConfiguration;
    protected Serde<JsonNode> jsonSerde;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    protected ConversationStateRepository conversationStateRepository;

    @Autowired
    protected QuestionGenerator questionGenerator;

    @PostConstruct
    public void init() {
        this.defaultStreamConfiguration = new Properties();
        defaultStreamConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperties.getKafkaHost());

        Serializer<JsonNode> jsonSerializer = new JsonSerializer();
        Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
        jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    }

    public void createQuestionStreamForConfig(JsonNode config, String questionTopic, String sendMessageTopic) {

        String streamName = config.get("name").asText() + "-question";

        Properties streamConfiguration = (Properties) defaultStreamConfiguration.clone();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, streamName);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> questionKStream = builder.stream(questionTopic, Consumed.with(Serdes.String(), jsonSerde));

        questionKStream.mapValues(chatNode -> {
            JsonNode nodeWithQuestion = questionGenerator.getQuestion(config, chatNode);

            conversationStateRepository.updateActiveNodeForConversation(chatNode.at(JsonPointerNameConstants.conversationId).asText(),
                    config.get("name").asText());

            return nodeWithQuestion;
        }).to(sendMessageTopic, Produced.with(Serdes.String(), jsonSerde));

        startStream(builder, streamConfiguration);

        log.info("Stream started : " + streamName + ", from : " + questionTopic + ", to : " + sendMessageTopic);
    }

    protected void startStream(StreamsBuilder builder, Properties streamConfiguration) {
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamConfiguration);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}
