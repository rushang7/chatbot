package org.egov.chat.service.restendpoint;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface RestEndpoint {

    public ObjectNode messageForRestCall(ObjectNode params) throws Exception;

}
