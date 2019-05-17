package org.egov.chat.xternal.restendpoint;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.chat.models.Message;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class PGRComplaintCreateTest {

    @Test
    public void test() throws IOException {
//        List<Message> messageList = new ArrayList<>();
//
//        messageList.add(Message.builder().nodeId("type").messageContent("1").build());
//        messageList.add(Message.builder().nodeId("address").messageContent("my address").build());
//
//        Optional<Message> message = messageList.stream().filter(message1 -> message1.getNodeId() == "type").findFirst();
//
//        System.out.println(message.get().getMessageContent());
//
//        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
//        ObjectNode objectNode = mapper.createObjectNode();
//
//        objectNode.set("asd", TextNode.valueOf("qwe"));
//
//        log.info(String.valueOf(objectNode.at("/asd")));
//
//        log.info(objectNode.toString());

        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

        String pgrCreateRequestBody = "{\"RequestInfo\":{\"authToken\":\"\"},\"actionInfo\":[{\"media\":[]}],\"services\":[{\"addressDetail\":{\"city\":\"\",\"mohalla\":\"\"},\"city\":\"\",\"mohalla\":\"\",\"phone\":\"\",\"serviceCode\":\"\",\"source\":\"web\",\"tenantId\":\"\"}]}";

        DocumentContext request = JsonPath.parse(pgrCreateRequestBody);

        DocumentContext requestInfo = JsonPath.parse( "{\n" +
                "    \"authToken\": \"986cbf03-1ee0-4346-8e29-e634812579f1\"\n" +
                "  }");

        log.info("RequestInfo : " + requestInfo.jsonString());

        request.set("$.RequestInfo",  requestInfo.json());

        log.info(request.jsonString());
    }

}
