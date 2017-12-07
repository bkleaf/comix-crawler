package com.bleaf.comix.crawler.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("bleaf.comix.crawler")
public class ComixConfig {
    String basePath;
}
