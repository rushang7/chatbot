package org.egov.chat.xternal.restendpoint;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.chat.service.restendpoint.RestEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@PropertySource("classpath:xternal.properties")
@Slf4j
@Component
public class PGRComplaintCreate implements RestEndpoint {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${egov.external.host}")
    private String egovExternalHost;

    @Value("${location.service.host}")
    private String locationServiceHost;
    @Value("${location.service.search.path}")
    private String locationServiceSearchPath;

    @Value("${pgr.service.host}")
    private String pgrHost;
    @Value("${pgr.service.create.path}")
    private String pgrCreateComplaintPath;

    String pgrCreateRequestBody = "{\"RequestInfo\":{\"authToken\":\"\", \"userInfo\": {}},\"actionInfo\":[{\"media\":[]}],\"services\":[{\"addressDetail\":{\"city\":\"\",\"mohalla\":\"\"},\"city\":\"\",\"mohalla\":\"\",\"phone\":\"\",\"serviceCode\":\"\",\"source\":\"web\",\"tenantId\":\"\"}]}";

    @Override
    public ObjectNode messageForRestCall(ObjectNode params) throws Exception {
        String authToken = params.get("authToken").asText();
        String refreshToken = params.get("refreshToken").asText();
        String tenantId = params.get("tenantId").asText();
        String mobileNumber = params.get("mobileNumber").asText();
        String complaintType = params.get("pgr.create.complaintType").asText();
        String locality = params.get("pgr.create.locality").asText();
        String complaintDetails = params.get("pgr.create.complaintDetails").asText();
        String address = params.get("pgr.create.address").asText();
        DocumentContext userInfo = JsonPath.parse(params.get("userInfo").asText());

        DocumentContext request = JsonPath.parse(pgrCreateRequestBody);

        request.set("$.RequestInfo.authToken", authToken);
        request.set("$.RequestInfo.userInfo",  userInfo.json());
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
            requestObject = objectMapper.readTree(request.jsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectNode responseMessage = objectMapper.createObjectNode();
        responseMessage.put("type", "text");
        try {
            ResponseEntity<ObjectNode> response = restTemplate.postForEntity(pgrHost + pgrCreateComplaintPath,
                    requestObject, ObjectNode.class);
            responseMessage = makeMessageForResponse(response, refreshToken);
        } catch (Exception e) {
            responseMessage.put("text", "Error occured");
        }
        return responseMessage;
    }

    private ObjectNode makeMessageForResponse(ResponseEntity<ObjectNode> responseEntity, String token) throws Exception {
        ObjectNode responseMessage = objectMapper.createObjectNode();
        responseMessage.put("type", "text");

        if(responseEntity.getStatusCode().is2xxSuccessful()) {
            ObjectNode pgrResponse = responseEntity.getBody();
            String serviceRequestId = pgrResponse.get("services").get(0).get("serviceRequestId").asText();
            String encodedPath = URLEncoder.encode( serviceRequestId, "UTF-8" );
            String url = egovExternalHost + "/citizen/complaint-details/" + encodedPath;
            url += "?token=" + token;

            String message = "Complaint registered successfully. Complaint Id is : " + serviceRequestId
                    + "\n You can view your complaint at : ";
            message += url;

            responseMessage.put("text", message);
        } else {
            responseMessage.put("text", "Error Occured");
        }
        return responseMessage;
    }

    private String getMohallaCode(String tenantId, String locality, String authToken) {

        String requestBodyString = "{\"RequestInfo\":{\"authToken\":\"\"}}";

        DocumentContext request = JsonPath.parse(requestBodyString);
        request.set("$.RequestInfo.authToken", authToken);

        Map<String, String> defaultQueryParams = new HashMap<String, String>() {{
            put("hierarchyTypeCode","ADMIN");
            put("boundaryType", "Locality");
        }};

        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromUriString(locationServiceHost + locationServiceSearchPath);
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
