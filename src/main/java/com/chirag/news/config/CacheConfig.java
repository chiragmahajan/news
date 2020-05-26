package com.chirag.news.config;

import com.chirag.news.config.appConfig.CacheDetailConfig;
import com.chirag.news.constants.Constants;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
    @Autowired
    private CacheDetailConfig cacheDetailConfig;

    @Bean(Constants.CAFFEINE_CACHE_MANAGER)
    public CacheManager cacheManager() {
        CaffeineCache loginCache = buildCache(Constants.LOGIN_CACHE,cacheDetailConfig.getLoginCacheTimeoutDays(),cacheDetailConfig.getLoginCachePageSize());
        CaffeineCache userNewsCache = buildCache(Constants.USER_NEWS_CACHE,cacheDetailConfig.getLoginCacheTimeoutDays(),cacheDetailConfig.getLoginCachePageSize());
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(Arrays.asList(loginCache,userNewsCache));
        return manager;
    }

    private CaffeineCache buildCache(String name, int daysToExpire, int maximumSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(daysToExpire, TimeUnit.DAYS)
                .maximumSize(maximumSize)
                .build());

    }

}
