package org.egov.chat.xternal.valuefetch;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minidev.json.JSONArray;
import org.egov.chat.config.ApplicationProperties;
import org.egov.chat.service.valuefetch.ExternalValueFetcher;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MdmsValueFetcher implements ExternalValueFetcher {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationProperties applicationProperties;

    private String mdmsURL;

    @PostConstruct
    public void init() {
        mdmsURL = applicationProperties.getEgovHost() + "/egov-mdms-service/v1/_search";
    }

    @Override
    public List<String> getValues(ObjectNode params) {
        String tenantIdArg = params.get("tenantId").asText();
        String moduleNameArg = params.get("moduleName").asText();
        String masterDetailsNameArg = params.get("masterDetailsName").asText();
        String filterArg = params.get("filter").asText();

        MasterDetail masterDetail = MasterDetail.builder().name(masterDetailsNameArg).filter(filterArg).build();
        ModuleDetail moduleDetail =
                ModuleDetail.builder().moduleName(moduleNameArg).masterDetails(Collections.singletonList(masterDetail)).build();
        MdmsCriteria mdmsCriteria =
                MdmsCriteria.builder().tenantId(tenantIdArg).moduleDetails(Collections.singletonList(moduleDetail)).build();
        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria).requestInfo(RequestInfo.builder().build()) .build();

//        MdmsClientService mdmsClientService = new MdmsClientService();
//        MdmsResponse mdmsResponse = mdmsClientService.getMaster(mdmsCriteriaReq);

        MdmsResponse mdmsResponse = restTemplate.postForObject(mdmsURL, mdmsCriteriaReq, MdmsResponse.class);

        Map<String, Map<String, JSONArray>> mdmsRes = mdmsResponse.getMdmsRes();

        JSONArray mdmsResValues = mdmsRes.get(moduleNameArg).get(masterDetailsNameArg);

        List<String> values = new ArrayList<>();

        for(Object mdmsResValue : mdmsResValues) {
            values.add((String) mdmsResValue);
        }

        return values;
    }
}
