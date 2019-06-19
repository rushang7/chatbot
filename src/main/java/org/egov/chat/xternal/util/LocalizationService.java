package org.egov.chat.xternal.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PropertySource("classpath:xternal.properties")
@Service
public class LocalizationService {

    @Value("${egov.localization.host}")
    private String localizationHost;
    @Value("${egov.localization.search.path}")
    private String localizationSearchPath;
    @Value("${state.level.tenant.id}")
    private String stateLevelTenantId;

    private String locale = "en_IN";

    private Map<String, String> defaultQueryParams = new HashMap<String, String>() {{
        put("locale",locale);
        put("tenantId", stateLevelTenantId);
    }};

    private Map<String,String>  codeToMessageMapping;
    private Map<String,String>  messageToCodeMapping;

    @PostConstruct
    public void init() {

    }

}
