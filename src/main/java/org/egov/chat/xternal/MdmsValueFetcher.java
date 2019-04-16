package org.egov.chat.xternal;

import org.egov.chat.valuefetch.ExternalValueFetcher;

import java.util.List;

public class MdmsValueFetcher implements ExternalValueFetcher {


    private String mdmsHost;
    private String mdmsSearchEndpoint;

    public MdmsValueFetcher(String mdmsHost, String mdmsSearchEndpoint) {
        this.mdmsHost = mdmsHost;
        this.mdmsSearchEndpoint = mdmsSearchEndpoint;
    }

    @Override
    public List<String> getValues(String ... args) {
        if(args.length != 4)
            throw new IllegalArgumentException();
        String tenantId = args[0];
        String moduleName = args[1];
        String masterDetailsName = args[2];
        String filter = args[3];



        return null;
    }
}
