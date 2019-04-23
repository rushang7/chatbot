package org.egov.chat.xternal.valuefetch;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.chat.service.valuefetch.ExternalValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LocalityValueFetcher implements ExternalValueFetcher {

    @Autowired
    private RestTemplate restTemplate;

    private String requestBodyString = "{\"RequestInfo\":{\"authToken\":\"954e5479-668b-4bf7-b149-e5c2bc7a72d1\"}}";
    private String locationServiceUrl = "https://egov-micro-dev.egovernments" +
            ".org/egov-location/location/v11/boundarys/_search";
    private Map<String, String> defaultQueryParams = new HashMap<String, String>() {{
        put("hierarchyTypeCode","ADMIN");
        put("boundaryType", "Locality");
    }};

    @Override
    public List<String> getValues(ObjectNode params) {
        String tenantId = params.get("tenantId").asText();

        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromUriString(locationServiceUrl);
        defaultQueryParams.forEach((key, value) -> uriComponents.queryParam(key, value));
        uriComponents.queryParam("tenantId", tenantId);

        String url = uriComponents.buildAndExpand().toUriString();

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        ObjectNode requestBody = null;
        try {
             requestBody = (ObjectNode) mapper.readTree(requestBodyString);
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
