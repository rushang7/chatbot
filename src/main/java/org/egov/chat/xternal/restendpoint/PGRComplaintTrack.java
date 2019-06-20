package org.egov.chat.xternal.restendpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.chat.service.restendpoint.RestEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@PropertySource("classpath:xternal.properties")
@Component
@Slf4j
public class PGRComplaintTrack implements RestEndpoint {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${egov.external.host}")
    private String egovExternalHost;

    @Value("${pgr.service.host}")
    private String pgrHost;
    @Value("${pgr.service.search.path}")
    private String pgrSearchComplaintPath;
    @Value("${pgr.recent.complaints.count}")
    private Integer numberOfRecentComplaints;

    String pgrRequestBody = "{\"RequestInfo\":{\"authToken\":\"\",\"userInfo\":\"\"}}";

    @Override
    public ObjectNode getMessageForRestCall(ObjectNode params) throws Exception {
        String tenantId = params.get("tenantId").asText();
        String authToken = params.get("authToken").asText();
        DocumentContext userInfo = JsonPath.parse(params.get("userInfo").asText());

        DocumentContext request = JsonPath.parse(pgrRequestBody);
        request.set("$.RequestInfo.authToken", authToken);
        request.set("$.RequestInfo.userInfo",  userInfo.json());

        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromUriString(pgrHost + pgrSearchComplaintPath);
        uriComponents.queryParam("tenantId", tenantId);
        uriComponents.queryParam("noOfRecords", numberOfRecentComplaints);

        JsonNode requestObject = null;
        try {
            requestObject = objectMapper.readTree(request.jsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectNode responseMessage = objectMapper.createObjectNode();
        responseMessage.put("type", "text");
        try {
            ResponseEntity<ObjectNode> response = restTemplate.postForEntity(uriComponents.buildAndExpand().toUri(),
                    requestObject, ObjectNode.class);
            responseMessage = makeMessageForResponse(response);

        } catch (Exception e) {
            responseMessage.put("text", "Error occured");
        }

        return responseMessage;
    }

    private ObjectNode makeMessageForResponse(ResponseEntity<ObjectNode> responseEntity) {

        ObjectNode responseMessage = objectMapper.createObjectNode();
        responseMessage.put("type", "text");

        if(responseEntity.getStatusCode().is2xxSuccessful()) {

            DocumentContext documentContext = JsonPath.parse(responseEntity.getBody().toString());

            documentContext.read("$.services.length()");

            Integer numberOfServices = (Integer) ( (JSONArray) documentContext.read("$..services.length()")) .get(0);

            String message = "";

            if(numberOfServices > 0) {
                message += "Complaint Details :";
                for (int i = 0; i < numberOfServices; i++) {
                    if(numberOfServices > 1)
                        message += "\n" + (i + 1) + ".";
                    else
                        message += "\n";
                    message += "Complaint Number : " + documentContext.read("$.services.[\" + i + \"].serviceRequestId");
                    message += "\nCategory : " + documentContext.read("$.services.[" + i + "].serviceCode");
                    Date createdDate = new Date((long) documentContext.read("$.services.[" + i + "].auditDetails.createdTime"));
                    message += "\nFiled Date : " + getDateFromTimestamp(createdDate);
                    message += "\nCurrent Status : " + documentContext.read("$.services.[" + i + "].status");
                }
            } else {
                message += "No complaints to display";
            }

            responseMessage.put("text", message);
        } else {
            responseMessage.put("text", "Error Occured");
        }

        return responseMessage;
    }


    private String getDateFromTimestamp(Date createdDate) {
        String pattern = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(createdDate);
    }
}
