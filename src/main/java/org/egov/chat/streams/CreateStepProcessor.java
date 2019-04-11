package org.egov.chat.streams;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.egov.chat.repository.ConversationStateRepository;
import org.egov.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class CreateStepProcessor {

    private Properties defaultConsumerConfiguration;

    @Autowired
    private ConversationStateRepository conversationStateRepository;
    @Autowired
    private MessageRepository messageRepository;

    public CreateStepProcessor(Properties defaultConsumerConfiguration) {
        this.defaultConsumerConfiguration = defaultConsumerConfiguration;
    }

    public void createQuestionStreamForConfig(JsonNode config, String questionTopic, String sendMessageTopic) {
        Properties consumerConfigurations = (Properties) defaultConsumerConfiguration.clone();

        consumerConfigurations.setProperty(ConsumerConfig.GROUP_ID_CONFIG, config.get("name").asText());
        consumerConfigurations.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfigurations.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());


        KafkaConsumer<String, JsonNode> kafkaConsumer = new KafkaConsumer<>(consumerConfigurations);

        kafkaConsumer.subscribe(Collections.singletonList(questionTopic));

        try {
            while (true) {
                ConsumerRecords<String, JsonNode> records = kafkaConsumer.poll(1000);

                for (ConsumerRecord<String, JsonNode> record : records) {
                    System.out.println(record.offset() + ": " + record.value());
                }
            }
        } finally {
            kafkaConsumer.close();
        }

    }

}
