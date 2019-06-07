package org.egov.chat.xternal.valuefetch;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.egov.chat.service.valuefetch.ExternalValueFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PropertySource("classpath:xternal.properties")
@Component
public class LocalityValueFetcher implements ExternalValueFetcher {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${location.service.host}")
    private String locationServiceHost;
    @Value("${location.service.search.path}")
    private String locationServiceSearchPath;

    private Map<String, String> defaultQueryParams = new HashMap<String, String>() {{
        put("hierarchyTypeCode","ADMIN");
        put("boundaryType", "Locality");
    }};

    private String requestBodyString = "{\"RequestInfo\":{\"authToken\":\"\"}}";

    @Override
    public List<String> getValues(ObjectNode params) {
        return extractLocalities(fetchValues(params));
    }

    @Override
    public String getCodeForValue(ObjectNode params, String value) {
        return getMohallaCode(fetchValues(params), value);
    }

    private ObjectNode fetchValues(ObjectNode params) {
        String tenantId = params.get("tenantId").asText();
        String authToken = params.get("authToken").asText();

        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromUriString(locationServiceHost + locationServiceSearchPath);
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

        return locationData;
    }

    List<String> extractLocalities(ObjectNode locationData) {
        List<String> localities = new ArrayList<>();

        ArrayNode boundries = (ArrayNode) locationData.get("TenantBoundary").get(0).get("boundary");

        for(JsonNode boundry : boundries) {
            localities.add(boundry.get("name").asText());
        }

        return localities;
    }

    private String getMohallaCode(ObjectNode locationData, String locality) {

        ArrayNode boundaryData = (ArrayNode) locationData.get("TenantBoundary").get(0).get("boundary");

        for(JsonNode boundary : boundaryData) {
            String currentLocalityName = boundary.get("name").asText();
            if(currentLocalityName.equalsIgnoreCase(locality)) {
                return boundary.get("code").asText();
            }
        }

        return "";
    }


}
