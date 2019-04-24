package org.egov.chat.xternal.restendpoint;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.egov.chat.models.Message;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PGRComplaintCreateTest {

    @Test
    public void test() throws IOException {
        List<Message> messageList = new ArrayList<>();

        messageList.add(Message.builder().nodeId("type").messageContent("1").build());
        messageList.add(Message.builder().nodeId("address").messageContent("my address").build());

        Optional<Message> message = messageList.stream().filter(message1 -> message1.getNodeId() == "type").findFirst();

        System.out.println(message.get().getMessageContent());

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        ObjectNode objectNode = mapper.createObjectNode();

        objectNode.set("asd", TextNode.valueOf("qwe"));

        System.out.println(objectNode.at("/asd"));


    }

}
