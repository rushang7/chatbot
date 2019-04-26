package org.egov.chat.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
@Getter
public class ApplicationProperties {

    @Value("${kafka.bootstrap.server}")
    private String kafkaHost;

    @Value("${egov.host.server}")
    private String egovHost;

}
