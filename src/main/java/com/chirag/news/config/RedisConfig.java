package com.chirag.news.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties("redis")
public class RedisConfig {

    private String type;
    private Integer port;
    private String host;
    private int connectionTimeout;
    private int readTimeout;
    private int poolSize;
    private boolean homeCacheEnable;
    private int cacheTimeout;
    private Boolean sentinelOn;
    private Set<String> sentinels;

    public Boolean getSentinelOn() {
        return sentinelOn;
    }

    public void setSentinelOn(Boolean sentinelOn) {
        this.sentinelOn = sentinelOn;
    }

    public Set<String> getSentinels() {
        return sentinels;
    }

    public void setSentinels(Set<String> sentinels) {
        this.sentinels = sentinels;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHost() { return host; }

    public void setHost(String host) {
        this.host = host;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getPoolSize() { return poolSize; }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public boolean isHomeCacheEnable() {
        return homeCacheEnable;
    }

    public void setHomeCacheEnable(boolean homeCacheEnable) {
        this.homeCacheEnable = homeCacheEnable;
    }

    public int getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }

    @Override
    public String toString() {
        return "RedisConfig{" +
                "type='" + type + '\'' +
                ", port=" + port +
                ", host='" + host + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", readTimeout=" + readTimeout +
                ", poolSize=" + poolSize +
                ", homeCacheEnable=" + homeCacheEnable +
                ", cacheTimeout=" + cacheTimeout +
                ", sentinelOn=" + sentinelOn +
                ", sentinels=" + sentinels +
                '}';
    }
}

