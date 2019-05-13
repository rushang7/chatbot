package org.egov.chat.post.formatter;

import com.fasterxml.jackson.databind.JsonNode;

public interface ResponseFormatter {

    public String getStreamName();

    public JsonNode getTransformedResponse(JsonNode response);

    public void startResponseStream(String inputTopic, String outputTopic);

}
