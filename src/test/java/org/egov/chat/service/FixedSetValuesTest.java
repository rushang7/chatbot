package org.egov.chat.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

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

    @Test
    public void testIntegerValuesForDifferentLocales() throws ParseException {
        NumberFormat nf = NumberFormat.getInstance();
        System.out.println(nf.parse("३३"));
        System.out.println(Integer.parseInt("३३"));
    }



}