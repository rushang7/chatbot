package org.egov.chat.xternal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PropertySource("classpath:xternal.properties")
@Service
public class LocalizationService {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${localization.service.host}")
    private String localizationHost;
    @Value("${localization.service.search.path}")
    private String localizationSearchPath;
    @Value("${state.level.tenant.id}")
    private String stateLevelTenantId;

    private String locale = "en_IN";

    private Map<String,String>  codeToMessageMapping;
    private Map<String,String>  messageToCodeMapping;

    @PostConstruct
    public void init() {
        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromUriString(localizationHost + localizationSearchPath);
        uriComponents.queryParam("locale", locale);
        uriComponents.queryParam("tenantId", stateLevelTenantId);

        ObjectNode localizationData = restTemplate.postForObject(uriComponents.buildAndExpand().toUriString(),
                objectMapper.createObjectNode(), ObjectNode.class);

        ArrayNode localizationMessages = (ArrayNode) localizationData.get("messages");

        initializeMaps(localizationMessages);
    }

    private void initializeMaps(ArrayNode localizationMessages) {
        codeToMessageMapping = new HashMap<>();
        messageToCodeMapping = new HashMap<>();

        for(JsonNode localizationMessage : localizationMessages) {
            String code = localizationMessage.get("code").asText();
            String message = localizationMessage.get("message").asText();

            codeToMessageMapping.put(code, message);
            messageToCodeMapping.put(message, code);
        }

        log.info("codeToMessageMapping initialized with : " + codeToMessageMapping.size() + " values");
        log.info("messageToCodeMapping initialized with : " + messageToCodeMapping.size() + " values");
    }

    public String getMessageForCode(String code) {
        return codeToMessageMapping.get(code);
    }

    public String getCodeForMessage(String message) {
        return messageToCodeMapping.get(message);
    }
}
