package com.bleaf.comix.crawler.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "bleaf.comix.marumaru")
public class MarumaruConfig {
    String baseUri;
    String dailyUri;
    String titleUri;
    String password;
    String dateFormat;
    String Cookie;
}