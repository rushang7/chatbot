package org.egov.chat.xternal.restendpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.net.URLEncoder;

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

    @Value("${pgr.service.host}")
    private String pgrHost;
    @Value("${pgr.service.create.path}")
    private String pgrCreateComplaintPath;

    String pgrCreateRequestBody = "{\"RequestInfo\":{\"authToken\":\"\", \"userInfo\": {}}," +
            "\"actionInfo\":[{\"media\":[]}],\"services\":[{\"addressDetail\":{\"city\":\"\",\"latitude\" : \"\",\"longitude\" : \"\"},\"city\":\"\",\"mohalla\":\"\",\"phone\":\"\",\"serviceCode\":\"\",\"source\":\"web\",\"tenantId\":\"\"}]}";

    @Override
    public ObjectNode getMessageForRestCall(ObjectNode params) throws Exception {
        String authToken = params.get("authToken").asText();
        String refreshToken = params.get("refreshToken").asText();
        String mobileNumber = params.get("mobileNumber").asText();
        String complaintType = params.get("pgr.create.complaintType").asText();
        String city = params.get("pgr.create.tenantId").asText();
//        String locality = params.get("pgr.create.locality").asText();
        String complaintDetails = params.get("pgr.create.complaintDetails").asText();
        DocumentContext userInfo = JsonPath.parse(params.get("userInfo").asText());

        String location = params.get("pgr.create.location").asText();
        ObjectNode locationNode = (ObjectNode) objectMapper.readTree(location);

        DocumentContext request = JsonPath.parse(pgrCreateRequestBody);

        request.set("$.RequestInfo.authToken", authToken);
        request.set("$.RequestInfo.userInfo",  userInfo.json());
        request.set("$.services.[0].city", city);
        request.set("$.services.[0].tenantId", city);
        request.set("$.services.[0].addressDetail.latitude", locationNode.get("latitude").asText());
        request.set("$.services.[0].addressDetail.longitude", locationNode.get("longitude").asText());
        request.set("$.services.[0].addressDetail.city", city);
//        request.set("$.services.[0].addressDetail.mohalla", locality);
        request.set("$.services.[0].serviceCode", complaintType);
        request.set("$.services.[0].phone", mobileNumber);
        request.set("$.services.[0].description", complaintDetails);

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

}
