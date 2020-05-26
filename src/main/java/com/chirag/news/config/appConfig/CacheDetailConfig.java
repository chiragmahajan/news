package com.chirag.news.config.appConfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("cache")
@Data
public class CacheDetailConfig {
    private int loginCacheTimeoutDays;
    private int loginCachePageSize;
}
