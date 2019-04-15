package org.egov.chat.segregation;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.chat.graph.TopicNameGetter;
import org.egov.chat.repository.ConversationStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InputSegregator {

    @Autowired
    private ConversationStateRepository conversationStateRepository;

    @Autowired
    private TopicNameGetter topicNameGetter;

    @Autowired
    private KafkaTemplate<String, JsonNode> kafkaTemplate;

    public void segregateAnswer(ConsumerRecord<String, JsonNode> consumerRecord) {
        JsonNode chatNode = consumerRecord.value();
        String conversationId = chatNode.get("conversationId").asText();

//        ConversationState conversationState = conversationStateRepository.getActiveNodeIdForConversation(conversationId);
//
//        String activeNodeId = conversationState.getActive_node_id();

        String activeNodeId = conversationStateRepository.getActiveNodeIdForConversation(conversationId);

        String topic = topicNameGetter.getAnswerInputTopicNameForNode(activeNodeId);

        kafkaTemplate.send(topic, consumerRecord.key(), chatNode);
    }
}
