package org.egov.chat.xternal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

@Slf4j
@PropertySource("classpath:xternal.properties")
@Component
public class FileStore {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${egov.filestore.host}")
    private String fileStoreHost;
    @Value("${egov.filestore.put.endpoint}")
    private String fileStorePutEndpoint;
    @Value("${egov.filestore.get.url.endpoint}")
    private String fileStoreGetEndpoint;

    public String downloadAndStore(String getLink, String tenantId, String module) {
        try {
            File tempFile = getFileAt(getLink);
            String fileStoreId = saveToFileStore(tempFile, tenantId, module);
            tempFile.delete();
            return fileStoreId;
        }catch (Exception e){
            log.error("Get File failed");
            log.error(e.getMessage());
        }
        return null;
    }

    public String saveToFileStore(File file, String tenantId, String module) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

        formData.add("tenantId", tenantId);
        formData.add("module", module);
        formData.add("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<ObjectNode> response = restTemplate.exchange (fileStoreHost + fileStorePutEndpoint,
                    HttpMethod.POST, request, ObjectNode.class);
            log.debug("File Store response : " + response.getBody().toString());

            return response.getBody().get("files").get(0).get("fileStoreId").asText();

        } catch (Exception e) {
            log.error("Error in file store save request");
            log.error(e.getMessage());
        }


        return null;
    }


    public File getFileForFileStoreId(String fileStoreId, String tenantId) throws IOException {
        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromUriString(fileStoreHost + fileStoreGetEndpoint);
        uriComponents.queryParam("tenantId", tenantId);
        uriComponents.queryParam("fileStoreIds", fileStoreId);
        String url = uriComponents.buildAndExpand().toUriString();

        ResponseEntity<ObjectNode> response = restTemplate.getForEntity(url, ObjectNode.class);

        String fileURL = response.getBody().get(fileStoreId).asText();

        return getFileAt(fileURL);
    }

    public File getFileAt(String getLink) throws IOException {
        File tempFile = new File(FilenameUtils.getName(getLink));

        URL url = new URL(getLink);
        FileUtils.copyURLToFile(url, tempFile);

        log.debug("Filename : " + tempFile.getName());

        return tempFile;
    }

    public String getBase64EncodedStringOfFile(File file) throws IOException {
        return new String(Base64.getEncoder().encode(FileUtils.readFileToByteArray(file)));
    }
}
