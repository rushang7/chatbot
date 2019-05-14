package org.egov.chat.xternal.restendpoint;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.chat.config.ApplicationProperties;
import org.egov.chat.service.restendpoint.RestEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PGRComplaintCreate implements RestEndpoint {

    @Autowired
    private RestTemplate restTemplate;
    private ObjectMapper mapper = new ObjectMapper(new JsonFactory());

    @Autowired
    private ApplicationProperties applicationProperties;


    private String pgrCreateComplaintUrl;
    private String locationServiceUrl;

    String pgrCreateRequestBody = "{\"RequestInfo\":{\"authToken\":\"\"},\"actionInfo\":[{\"media\":[]}],\"services\":[{\"addressDetail\":{\"city\":\"\",\"mohalla\":\"\"},\"city\":\"\",\"mohalla\":\"\",\"phone\":\"\",\"serviceCode\":\"\",\"source\":\"web\",\"tenantId\":\"\"}]}";

    @PostConstruct
    public void init() {
        pgrCreateComplaintUrl = applicationProperties.getEgovHost() + "/rainmaker-pgr/v1/requests/_create";
        locationServiceUrl = applicationProperties.getEgovHost() + "/egov-location/location/v11/boundarys/_search";
    }

    @Override
    public String messageForRestCall(ObjectNode params) {
        String authToken = params.get("authToken").asText();
        String refreshToken = params.get("refreshToken").asText();
        String tenantId = params.get("tenantId").asText();
        String mobileNumber = params.get("mobileNumber").asText();
        String complaintType = params.get("pgr.create.complaintType").asText();
        String locality = params.get("pgr.create.locality").asText();
        String complaintDetails = params.get("pgr.create.complaintDetails").asText();
        String address = params.get("pgr.create.address").asText();
        DocumentContext userInfo = JsonPath.parse(params.get("userInfo").toString());

        DocumentContext request = JsonPath.parse(pgrCreateRequestBody);

        request.set("$.RequestInfo.authToken", authToken);
        request.set("$.RequestInfo.userInfo", userInfo);
        request.set("$.services.[0].addressDetail.city", tenantId);
        request.set("$.services.[0].city", tenantId);
        request.set("$.services.[0].tenantId", tenantId);
        request.set("$.services.[0].addressDetail.mohalla", getMohallaCode(tenantId, locality, authToken));
        request.set("$.services.[0].serviceCode", complaintType);
        request.set("$.services.[0].phone", mobileNumber);
        request.set("$.services.[0].description", complaintDetails);
        request.set("$.services.[0].addressDetail.houseNoAndStreetName", address);

        log.info("PGR Create complaint request : " + request.jsonString());

        JsonNode requestObject = null;
        try {
            requestObject = mapper.readTree(request.jsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ResponseEntity<ObjectNode> response = restTemplate.postForEntity(pgrCreateComplaintUrl, requestObject, ObjectNode.class);
            return makeMessageForResponse(response, refreshToken);
        } catch (Exception e) {
            return "Error occurred";
        }
    }

    private String makeMessageForResponse(ResponseEntity<ObjectNode> responseEntity, String token) throws Exception {
        if(responseEntity.getStatusCode().is2xxSuccessful()) {
            ObjectNode pgrResponse = responseEntity.getBody();
            String serviceRequestId = pgrResponse.get("services").get(0).get("serviceRequestId").asText();
            String encodedPath = URLEncoder.encode( serviceRequestId, "UTF-8" );
            String url = applicationProperties.getEgovHost() + "/citizen/complaint-details/" + encodedPath;
            url += "?token=" + token;

            String message = "Complain registered successfully. You can see your complain at : ";
            message += url;

            return message;
        } else {
            throw new Exception("Error occured");
        }
    }

    private String getMohallaCode(String tenantId, String locality, String authToken) {

        String requestBodyString = "{\"RequestInfo\":{\"authToken\":\"\"}}";

        DocumentContext request = JsonPath.parse(requestBodyString);
        request.set("$.RequestInfo.authToken", authToken);

        Map<String, String> defaultQueryParams = new HashMap<String, String>() {{
            put("hierarchyTypeCode","ADMIN");
            put("boundaryType", "Locality");
        }};

        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromUriString(locationServiceUrl);
        defaultQueryParams.forEach((key, value) -> uriComponents.queryParam(key, value));
        uriComponents.queryParam("tenantId", tenantId);

        String url = uriComponents.buildAndExpand().toUriString();

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        ObjectNode requestBody = null;
        try {
            requestBody = (ObjectNode) mapper.readTree(request.jsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectNode locationData = restTemplate.postForObject(url, requestBody, ObjectNode.class);

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
