package org.egov.chat.valuefetch;

import java.util.List;

public interface ExternalValueFetcher {

    public List<String> getValues(String ... args);

}
