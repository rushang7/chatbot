package org.egov.chat.xternal.restendpoint;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

@Slf4j
public class PGRComplaintTrackTest {

    private ObjectMapper objectMapper;

    @Before
    public void init() {
        objectMapper = new ObjectMapper(new JsonFactory());
    }

    @Test
    public void testDate() {
        String message = "";

        String pattern = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        Date createdDate = new Date(1559414278168L);

        String date = simpleDateFormat.format(createdDate);
        System.out.println(date);


        log.info(date.toString());
    }

    @Test
    public void testURI() throws UnsupportedEncodingException {
        String url = "http://{enpointUrl}?method=logout&session={sessionId}";
        URI expanded = new UriTemplate(url).expand("asd.com/asd", "30/06"); // this is what RestTemplate uses
        url = URLDecoder.decode(expanded.toString(), "UTF-8"); // java.net class
        log.debug(url);


    }

}