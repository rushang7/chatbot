package org.egov.chat.pre.enrichment;

import java.util.Map;

public class TenantIdEnricher {

    private Map<String, String> numberToTenantId;

    public String getTenantIdFor(String recepientNumber) {
        return numberToTenantId.get(recepientNumber);
    }

}
