package org.egov.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ComponentScan("org.egov.chat")
@PropertySource("classpath:application.properties")
public class AppConfig {


}
