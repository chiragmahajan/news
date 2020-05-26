package com.chirag.news.component;

import com.chirag.news.constants.Constants;
import com.chirag.news.model.cache.CacheBasicPutRequest;
import com.chirag.news.model.cache.CacheRequest;
import com.chirag.news.model.entity.News;
import com.chirag.news.repository.MyHomeNewsRepository;
import com.chirag.news.service.CacheService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NewsComponent {
    private Logger LOGGER = LoggerFactory.getLogger(LikeComponent.class);

    @Autowired
    private MyHomeNewsRepository myHomeNewsRepository;

    @Autowired
    private CacheService cacheService;

    private static Map<Long, News> allNews;

    private static Map<String,List<News>> userNews;

    private static final ObjectMapper mapper ;

    private static final String ALL_NEWS = "ALL_NEWS::";
    private static final String USER_NEWS = "USER_NEWS::";

    private static final Map<String,Integer> ttlMap;

    static {
        mapper = new ObjectMapper();
        ttlMap = new HashMap<>();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        ttlMap.put(Constants.CACHE_ALL_LABELS_KEY,21600);
    }

    @PostConstruct
    public void init() throws Exception {
        initialiseNewsCache();
    }

    public void initialiseNewsCache() throws Exception {

        allNews = new HashMap<>();
        userNews = new HashMap<>();
        List<News> everyNews = myHomeNewsRepository.getAllNews();
        for(News news:everyNews){
            allNews.put(news.getId(),news);
            if(userNews.get(news.getLogin().getUsername())==null){
                List<News> newsList = new ArrayList<>();
                newsList.add(news);
                userNews.put(news.getLogin().getUsername(),newsList);
            }else{
                List<News> newsList = userNews.get(news.getLogin().getUsername());
                newsList.add(news);
                userNews.put(news.getLogin().getUsername(),newsList);
            }

        }
        CacheBasicPutRequest<Map<Long,News>> cacheBasicPutRequest = new CacheBasicPutRequest<>();
        cacheBasicPutRequest.setNamespace(ALL_NEWS);
        cacheBasicPutRequest.setKey(Constants.ALL_NEWS_KEY);
        cacheBasicPutRequest.setTtl(ttlMap.getOrDefault(Constants.CACHE_ALL_LABELS_KEY,300));
        cacheBasicPutRequest.setUpdateTtl(true);
        cacheBasicPutRequest.setValue(allNews);
        if(!CollectionUtils.isEmpty(allNews)){
            cacheService.put(cacheBasicPutRequest);
        }
        CacheBasicPutRequest<Map<String,List<News>>> cacheBasicPutRequest1 = new CacheBasicPutRequest<>();
        cacheBasicPutRequest1.setNamespace(USER_NEWS);
        cacheBasicPutRequest1.setKey(Constants.USER_NEWS_KEY);
        cacheBasicPutRequest1.setTtl(ttlMap.getOrDefault(Constants.CACHE_ALL_LABELS_KEY,300));
        cacheBasicPutRequest1.setUpdateTtl(true);
        cacheBasicPutRequest1.setValue(userNews);
        if(!CollectionUtils.isEmpty(userNews)){
            cacheService.put(cacheBasicPutRequest1);
        }
    }

    public Map<Long,News> getAllNews() throws Exception {
        CacheRequest cacheRequest = new CacheRequest();
        cacheRequest.setNamespace(ALL_NEWS);
        cacheRequest.setKey(Constants.ALL_NEWS_KEY);
        Map<Long,News> newsAll;
        try {
            newsAll = cacheService.get(cacheRequest, new TypeReference<Map<Long,News>>() {});
            if(CollectionUtils.isEmpty(newsAll)){
                throw new Exception();
            }
            return newsAll;
        }catch (Exception e){
            LOGGER.error("NewsComponent.getAllNews:  Could not fetch news mappings from cache",e);
        }
        initialiseNewsCache();
        return allNews;
    }

    public Map<String,List<News>> getUserNews() throws Exception {
        CacheRequest cacheRequest = new CacheRequest();
        cacheRequest.setNamespace(USER_NEWS);
        cacheRequest.setKey(Constants.USER_NEWS_KEY);
        Map<String,List<News>> newsAll;
        try {
            newsAll = cacheService.get(cacheRequest, new TypeReference<Map<String,List<News>>>() {});
            if(CollectionUtils.isEmpty(newsAll)){
                throw new Exception();
            }
            return newsAll;
        }catch (Exception e){
            LOGGER.error("NewsComponent.getAllNewsOfUser:  Could not fetch news mappings from cache",e);
        }
        initialiseNewsCache();
        return userNews;
    }
}
