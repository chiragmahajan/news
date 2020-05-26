package com.chirag.news.config.appConfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("jwt")
@Data
public class JwtConfig {
    private String clientId;
    private String secretKey;
    private Long leewayWindowIssuedAt;
    private Long expiryTime;
}
