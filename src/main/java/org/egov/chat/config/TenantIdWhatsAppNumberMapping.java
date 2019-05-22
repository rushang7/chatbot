package org.egov.chat.config;

import org.springframework.stereotype.Component;

@Component
public class TenantIdWhatsAppNumberMapping {

    // TODO : Remove hard-coded mapping

    public String getTenantIdForNumber(String number) {
        return "pb.amritsar";
    }

    public String getNumberForTenantId(String tenantId) {
        return "919845315868";
    }
}
