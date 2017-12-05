package com.bleaf.comix.crawler.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "bleaf.comix.url")
public class ComixUrlConfig {
    String marumaruDailyUrl;
    String marumaruTitleUrl;
    String marumaruPassword;
}
