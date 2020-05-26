package com.chirag.news.config.appConfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("db")
@Data
public class DBConfig {
    private String masterUrl;
    private String masterPassword;
    private String masterUsername;
    private String masterDriverClass;
    private String masterPoolName;
    private int masterMaxPoolSize;
    private int masterMinIdle;
    private long masterConnectionTimeout;
    private String masterHibernateDilect;
    private String masterHibernateDdlAuto;
    private boolean masterHibernateShowSql;
    private long masterIdleConnTimeout;


    private String slaveUrl;
    private String slavePassword;
    private String slaveUsername;
    private String slaveDriverClass;
    private String slavePoolName;
    private int slaveMaxPoolSize;
    private int slaveMinIdle;
    private long slaveConnectionTimeout;
    private String slaveHibernateDilect;
    private String slaveHibernateDdlAuto;
    private boolean slaveHibernateShowSql;
    private long slaveIdleConnTimeout;
}
