package org.egov.chat.streams;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class CreateStepStreamTest {

    private ObjectMapper objectMapper;

    @Before
    public void init() {
        objectMapper = new ObjectMapper(new JsonFactory());
    }

    @Test
    public void test() {

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.set("asd", IntNode.valueOf(1));

        System.out.println(objectNode.get("asd").asText());


    }

}