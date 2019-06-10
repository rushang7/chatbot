package org.egov.chat.xternal.valuefetch;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minidev.json.JSONArray;
import org.egov.chat.service.valuefetch.ExternalValueFetcher;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class TenantIdValueFetcher implements ExternalValueFetcher {

    @Autowired
    private RestTemplate restTemplate;

    private String moduleName = "tenant";
    private String masterDetailsName = "tenants";

    @Value("${mdms.service.host}")
    private String mdmsHost;
    @Value("${mdms.service.search.path}")
    private String mdmsSearchPath;


    @Override
    public List<String> getValues(ObjectNode params) {
        return getCityName(fetchMdmsData(params));
    }

    @Override
    public String getCodeForValue(ObjectNode params, String value) {
        return getTenantIdCode(fetchMdmsData(params), value);
    }

    private JSONArray fetchMdmsData(ObjectNode params) {
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

        return mdmsResValues;
    }

    List<String> getCityName(JSONArray mdmsResValues) {
        List<String> values = new ArrayList<>();

        for(Object mdmsResValue : mdmsResValues) {
            HashMap mdmsValue = (HashMap) mdmsResValue;
            values.add(mdmsValue.get("name").toString());
        }

        return values;
    }

    String getTenantIdCode(JSONArray mdmsResValues, String cityName) {
        String tenantIdCode = "";
        for(Object mdmsResValue : mdmsResValues) {
            HashMap mdmsValue = (HashMap) mdmsResValue;
            if(mdmsValue.get("name").toString().equalsIgnoreCase(cityName)) {
                return mdmsValue.get("code").toString();
            }
        }
        return tenantIdCode;
    }
}
