package org.egov.chat.streams;

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
import org.egov.chat.models.Message;
import org.egov.chat.repository.ConversationStateRepository;
import org.egov.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;
import java.util.UUID;

public class CreateStepStream {

    private Properties defaultStreamConfiguration;

    @Autowired
    private ConversationStateRepository conversationStateRepository;
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    public CreateStepStream() {
        this.defaultStreamConfiguration = new Properties();
        defaultStreamConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    }

    public void createQuestionStreamForConfig(JsonNode config, String questionTopic, String sendMessageTopic) {

        Properties streamConfiguration = (Properties) defaultStreamConfiguration.clone();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, config.get("name").asText());

        Serializer<JsonNode> jsonSerializer = new JsonSerializer();
        Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
        Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> questionKStream = builder.stream(questionTopic, Consumed.with(Serdes.String(), jsonSerde));

        questionKStream.mapValues(chatNode -> {
            ObjectNode nodeWithQuestion = chatNode.deepCopy();
            nodeWithQuestion.set("question", TextNode.valueOf(config.get("message").asText()));

            conversationStateRepository.updateActiveNodeForConversation(chatNode.get("conversation_id").asText(),
                    config.get("name").asText());

            return (JsonNode) nodeWithQuestion;
        }).to(sendMessageTopic, Produced.with(Serdes.String(), jsonSerde));

        startStream(builder, streamConfiguration);
    }

    public void createEvaluateAnswerStreamForConfig(JsonNode config, String answerInputTopic, String answerOutputTopic, String questionTopic) {
        Properties streamConfiguration = (Properties) defaultStreamConfiguration.clone();
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, config.get("name").asText());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> answerKStream = builder.stream(answerInputTopic);

        KStream<String, JsonNode>[] branches = answerKStream.branch(
                (key, value) -> CreateStepStream.this.validate(value),
                (key, value) -> true
        );

        branches[0].mapValues(chatNode -> {

            String node_id = config.get("name").asText();
            String conversation_id = chatNode.get("conversation_id").asText();
            String messageContent = chatNode.get("answer").asText();

            Message message = Message.builder().message_id(UUID.randomUUID().toString())
                    .conversation_id(conversation_id).node_id(node_id).message_content(messageContent).build();

            messageRepository.insertMessage(message);
            return chatNode;
        }).to(answerOutputTopic);

        branches[1].mapValues(value -> value).to(questionTopic);

        startStream(builder, streamConfiguration);
    }

    private boolean validate(JsonNode value) {
        return true;
    }

    private void startStream(StreamsBuilder builder, Properties streamConfiguration) {
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamConfiguration);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}
