package com.chirag.news.component;

import com.chirag.news.constants.Constants;
import com.chirag.news.model.cache.CacheBasicPutRequest;
import com.chirag.news.model.cache.CacheRequest;
import com.chirag.news.model.entity.Likes;
import com.chirag.news.repository.LikeRepository;
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
public class LikeComponent {

    private Logger LOGGER = LoggerFactory.getLogger(LikeComponent.class);

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CacheService cacheService;

    private static Map<String, List<Likes>> likedNewsByUser;

    private static Map<Long,Long> numberOfLikesOnNews;

    private static final ObjectMapper mapper ;

    private static final String LIKED_NEWS_BY_USER_NAMESPACE = "USER_LIKED::";
    private static final String NEWS_LIKES_NAMESPACE = "NEWS_LIKES::";

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
        initialiseLikeCache();
    }

    public void initialiseLikeCache() throws Exception {

        likedNewsByUser = new HashMap<>();
        numberOfLikesOnNews = new HashMap<>();
        List<Likes> likesList = likeRepository.getAllLikes();
        for(Likes likes:likesList){
            if(likedNewsByUser.get(likes.getLogin().getUsername())==null){
                List<Likes> news = new ArrayList<>();
                news.add(likes);
                likedNewsByUser.put(likes.getLogin().getUsername(),news);
            }else{
                List<Likes> news = likedNewsByUser.get(likes.getLogin().getUsername());
                news.add(likes);
                likedNewsByUser.put(likes.getLogin().getUsername(),news);
            }
            if(numberOfLikesOnNews.get(likes.getNews().getId())==null){
                numberOfLikesOnNews.put(likes.getNews().getId(),1L);
            }else{
                numberOfLikesOnNews.put(likes.getNews().getId(),numberOfLikesOnNews.get(likes.getNews().getId())+1);
            }
        }
        CacheBasicPutRequest<Map<String,List<Likes>>> cacheBasicPutRequest = new CacheBasicPutRequest<>();
        cacheBasicPutRequest.setNamespace(LIKED_NEWS_BY_USER_NAMESPACE);
        cacheBasicPutRequest.setKey(Constants.LIKED_NEWS_BY_USER_KEY);
        cacheBasicPutRequest.setTtl(ttlMap.getOrDefault(Constants.CACHE_ALL_LABELS_KEY,300));
        cacheBasicPutRequest.setUpdateTtl(true);
        cacheBasicPutRequest.setValue(likedNewsByUser);
        if(!CollectionUtils.isEmpty(likedNewsByUser)){
            cacheService.put(cacheBasicPutRequest);
        }
        CacheBasicPutRequest<Map<Long,Long>> cacheBasicPutRequest1 = new CacheBasicPutRequest<>();
        cacheBasicPutRequest1.setNamespace(NEWS_LIKES_NAMESPACE);
        cacheBasicPutRequest1.setKey(Constants.TOTAL_LIKES_KEY);
        cacheBasicPutRequest1.setTtl(ttlMap.getOrDefault(Constants.CACHE_ALL_LABELS_KEY,300));
        cacheBasicPutRequest1.setUpdateTtl(true);
        cacheBasicPutRequest1.setValue(numberOfLikesOnNews);
        if(!CollectionUtils.isEmpty(numberOfLikesOnNews)){
            cacheService.put(cacheBasicPutRequest1);
        }
    }

    public Map<String,List<Likes>> getAllLikesOfUser() throws Exception {
        CacheRequest cacheRequest = new CacheRequest();
        cacheRequest.setNamespace(LIKED_NEWS_BY_USER_NAMESPACE);
        cacheRequest.setKey(Constants.LIKED_NEWS_BY_USER_KEY);
        Map<String,List<Likes>> likedNewsOfUser;
        try {
            likedNewsOfUser = cacheService.get(cacheRequest, new TypeReference<Map<String,List<Likes>>>() {});
            if(CollectionUtils.isEmpty(likedNewsOfUser)){
                throw new Exception();
            }
            return likedNewsOfUser;
        }catch (Exception e){
            LOGGER.error("LikeComponent.getAllLikesOfUser:  Could not fetch like mappings from cache",e);
        }
        initialiseLikeCache();
        return likedNewsByUser;
    }

    public Map<Long,Long> getTotalLikesOnNews() throws Exception {
        CacheRequest cacheRequest = new CacheRequest();
        cacheRequest.setNamespace(NEWS_LIKES_NAMESPACE);
        cacheRequest.setKey(Constants.TOTAL_LIKES_KEY);
        Map<Long, Long> totalLikes;
        try {
            totalLikes = cacheService.get(cacheRequest, new TypeReference<Map<Long,Long>>() {
            });
            if (CollectionUtils.isEmpty(totalLikes)) {
                throw new Exception();
            }
            return totalLikes;
        } catch (Exception e) {
            LOGGER.error("LikeComponent.totalLikesOnNews:  Could not fetch total likes mappings from cache", e);
        }
        initialiseLikeCache();
        return numberOfLikesOnNews;
    }
}
