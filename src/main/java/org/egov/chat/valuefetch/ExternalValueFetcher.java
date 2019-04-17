package org.egov.chat.valuefetch;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public interface ExternalValueFetcher {

    public List<String> getValues(ObjectNode params);

}
