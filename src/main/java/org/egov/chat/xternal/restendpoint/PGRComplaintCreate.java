package org.egov.chat.xternal.restendpoint;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.egov.chat.restendpoint.RestEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Component
public class PGRComplaintCreate implements RestEndpoint {


    @Autowired
    private RestTemplate restTemplate;
    private ObjectMapper mapper = new ObjectMapper(new JsonFactory());

    private String authToken = "954e5479-668b-4bf7-b149-e5c2bc7a72d1";
    private String host = "https://egov-micro-dev.egovernments.org";

    private String pgrCreateComplaintUrl = host + "/rainmaker-pgr/v1/requests/_create";

    private String locationServiceUrl = host + "/egov-location/location/v11/boundarys/_search";

    String pgrCreateRequestBody = "{\"RequestInfo\":{\"authToken\":\"954e5479-668b-4bf7-b149-e5c2bc7a72d1\"},\"actionInfo\":[{\"media\":[]}],\"services\":[{\"addressDetail\":{\"city\":\"pb.amritsar\",\"mohalla\":\"SUN04\"},\"city\":\"pb.amritsar\",\"mohalla\":\"SUN04\",\"phone\":\"9428010077\",\"serviceCode\":\"illegalDischargeOfSewage\",\"source\":\"web\",\"tenantId\":\"pb.amritsar\"}]}";

    @Override
    public String messageForRestCall(ObjectNode params) {
        String tenantId = params.get("tenantId").asText();
        String mobileNumber = params.get("mobileNumber").asText();
        String complaintType = params.get("pgr.create.complaintType").asText();
        String locality = params.get("pgr.create.locality").asText();
        String complaintDetails = params.get("pgr.create.complaintDetails").asText();
        String address = params.get("pgr.create.address").asText();


        DocumentContext request = JsonPath.parse(pgrCreateRequestBody);

        request.set("$.services.[0].addressDetail.city", tenantId);
        request.set("$.services.[0].city", tenantId);
        request.set("$.services.[0].tenantId", tenantId);
        request.set("$.services.[0].addressDetail.mohalla", getMohallaCode(tenantId, locality));
        request.set("$.services.[0].serviceCode", complaintType);
        request.set("$.services.[0].phone", mobileNumber);
        request.set("$.services.[0].description", complaintDetails);
        request.set("$.services.[0].addressDetail.houseNoAndStreetName", address);

        JsonNode requestObject = null;
        try {
            requestObject = mapper.readTree(request.jsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ResponseEntity<ObjectNode> response = restTemplate.postForEntity(pgrCreateComplaintUrl, requestObject, ObjectNode.class);
            return makeMessageForResponse(response);
        } catch (Exception e) {
            return "Error occurred";
        }
    }

    private String makeMessageForResponse(ResponseEntity<ObjectNode> responseEntity) throws UnsupportedEncodingException {
        if(responseEntity.getStatusCode().is2xxSuccessful()) {
            ObjectNode pgrResponse = responseEntity.getBody();
            String serviceRequestId = pgrResponse.get("services").get(0).get("serviceRequestId").asText();
            String encodedPath = URLEncoder.encode( serviceRequestId, "UTF-8" );
            String baseUrl = host + "/citizen/complaint-details/" + encodedPath;

            String message = "Complain registered successfully. You can see your complain at : ";
            message += baseUrl;

            return message;
        } else {
            return "Error occurred";
        }
    }

    private String getMohallaCode(String tenantId, String locality) {

        String requestBodyString = "{\"RequestInfo\":{\"authToken\":\"954e5479-668b-4bf7-b149-e5c2bc7a72d1\"}}";

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
            requestBody = (ObjectNode) mapper.readTree(requestBodyString);
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
