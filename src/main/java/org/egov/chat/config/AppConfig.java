package org.egov.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ComponentScan("org.egov.chat")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
