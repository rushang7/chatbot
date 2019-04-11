package org.egov.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.egov.chat.streams.CreateStepProcessor;
import org.egov.chat.streams.CreateStepStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.Properties;


public class ChatBot {

    @Autowired
    private static CreateStepStream createStepStream;

    public static void main(String args[]) throws Exception {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        JsonNode config = mapper.readTree(ChatBot.class.getClassLoader().getResource("graph/pgr/create/pgr.create.address.yaml"));

//        Properties defaultStreamConfiguration = new Properties();
//        defaultStreamConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        CreateStepStream createStepStream = new CreateStepStream();
        createStepStream.createQuestionStreamForConfig(config, "address-question", "send-message");



    }

}
