package org.egov.chat.xternal.valuefetch;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.egov.chat.config.ApplicationProperties;
import org.egov.chat.service.valuefetch.ExternalValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LocalityValueFetcher implements ExternalValueFetcher {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationProperties applicationProperties;

    private String locationServiceUrl;

    private Map<String, String> defaultQueryParams = new HashMap<String, String>() {{
        put("hierarchyTypeCode","ADMIN");
        put("boundaryType", "Locality");
    }};

    private String requestBodyString = "{\"RequestInfo\":{\"authToken\":\"\"}}";

    @PostConstruct
    public void init() {
        locationServiceUrl = applicationProperties.getEgovHost() + "/egov-location/location/v11/boundarys/_search";
    }

    @Override
    public List<String> getValues(ObjectNode params) {
        String tenantId = params.get("tenantId").asText();
        String authToken = params.get("authToken").asText();

        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromUriString(locationServiceUrl);
        defaultQueryParams.forEach((key, value) -> uriComponents.queryParam(key, value));
        uriComponents.queryParam("tenantId", tenantId);

        String url = uriComponents.buildAndExpand().toUriString();

        DocumentContext request = JsonPath.parse(requestBodyString);
        request.set("$.RequestInfo.authToken", authToken);

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        ObjectNode requestBody = null;
        try {
             requestBody = (ObjectNode) mapper.readTree(request.jsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectNode locationData = restTemplate.postForObject(url, requestBody, ObjectNode.class);

        return extractLocalities(locationData);
    }

    List<String> extractLocalities(ObjectNode locationData) {
        List<String> localities = new ArrayList<>();

        ArrayNode boundries = (ArrayNode) locationData.get("TenantBoundary").get(0).get("boundary");

        for(JsonNode boundry : boundries) {
            localities.add(boundry.get("name").asText());
        }

        return localities;
    }


}
