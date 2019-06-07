package org.egov.chat.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.chat.models.ConversationState;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class FixedSetValuesTest {

    @Test
    public void testArrayNodeToList() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

        String question = "{\"allValues\":[\"qwe\",\"asd\",\"zxc\"],\"askedValues\":[\"qwe\",\"asd\"]}";
        JsonNode questionDetails = objectMapper.readTree(question);

        List<String> validValues = objectMapper.readValue(questionDetails.get("askedValues").toString(), List.class);

        log.info(String.valueOf(validValues));

    }

}