package org.egov.chat.service.restendpoint;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface RestEndpoint {

    public String messageForRestCall(ObjectNode params) throws Exception;

}
