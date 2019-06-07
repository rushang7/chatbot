package org.egov.chat.xternal.valuefetch;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.egov.chat.config.ApplicationProperties;
import org.egov.chat.service.valuefetch.ExternalValueFetcher;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@PropertySource("classpath:xternal.properties")
@Component
public class ComplainTypeValueFetcher implements ExternalValueFetcher {

    @Autowired
    private RestTemplate restTemplate;

    private String moduleName = "RAINMAKER-PGR";
    private String masterDetailsName = "ServiceDefs";

    @Value("${mdms.service.host}")
    private String mdmsHost;
    @Value("${mdms.service.search.path}")
    private String mdmsSearchPath;

    @Override
    public List<String> getValues(ObjectNode params) {
        String tenantIdArg = params.get("tenantId").asText();

        MasterDetail masterDetail = MasterDetail.builder().name(masterDetailsName).build();
        ModuleDetail moduleDetail =
                ModuleDetail.builder().moduleName(moduleName).masterDetails(Collections.singletonList(masterDetail)).build();
        MdmsCriteria mdmsCriteria =
                MdmsCriteria.builder().tenantId(tenantIdArg).moduleDetails(Collections.singletonList(moduleDetail)).build();
        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria).requestInfo(RequestInfo.builder().build()) .build();

        MdmsResponse mdmsResponse = restTemplate.postForObject(mdmsHost + mdmsSearchPath, mdmsCriteriaReq, MdmsResponse.class);

        Map<String, Map<String, JSONArray>> mdmsRes = mdmsResponse.getMdmsRes();

        JSONArray mdmsResValues = mdmsRes.get(moduleName).get(masterDetailsName);

        List<String> values = getActiveComplaintTypes(mdmsResValues);

        return values;
    }

    @Override
    public String getCodeForValue(ObjectNode params, String value) {
        return value;
    }

    List<String> getActiveComplaintTypes(JSONArray mdmsResValues) {
        List<String> values = new ArrayList<>();

        for(Object mdmsResValue : mdmsResValues) {
            HashMap mdmsValue = (HashMap) mdmsResValue;
            if(mdmsValue.get("active").toString().equalsIgnoreCase("true")) {
                values.add(mdmsValue.get("serviceCode").toString());
            }
        }

        return values;
    }

}
