package org.egov.chat.post.systeminitiated;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface SystemInitiatedEventFormatter {

    public String getStreamName() ;

    public void startStream(String inputTopic, String outputTopic) ;

    public JsonNode createChatNode(JsonNode event) throws Exception;
}
