package com.chirag.news.component;

import com.chirag.news.constants.Constants;
import com.chirag.news.model.cache.CacheBasicPutRequest;
import com.chirag.news.model.cache.CacheRequest;
import com.chirag.news.model.entity.BookMarked;
import com.chirag.news.model.entity.Likes;
import com.chirag.news.repository.BookmarkRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BookMarkComponent {

    private Logger LOGGER = LoggerFactory.getLogger(BookMarkComponent.class);

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private CacheService cacheService;

    private static Map<String, Map<Long, BookMarked>> bookMarkedNews;

    private static final ObjectMapper mapper ;

    private static final String BOOKMARK_NAMESPACE = "BOOKMARK::";
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
        initialiseBookmarkCache();
    }

    public void initialiseBookmarkCache() throws Exception {

        bookMarkedNews = new HashMap<>();
        List<BookMarked> bookMarkedList = bookmarkRepository.getAll();
        for(BookMarked bookMarked:bookMarkedList){
            if(bookMarkedNews.get(bookMarked.getLogin().getUsername())==null){
                Map<Long,BookMarked> bookMarkedMap = new HashMap<>();
                bookMarkedMap.put(bookMarked.getNews().getId(),bookMarked);
                bookMarkedNews.put(bookMarked.getLogin().getUsername(),bookMarkedMap);
            }else{
                Map<Long,BookMarked> bookMarkedMap = bookMarkedNews.get(bookMarked.getLogin().getUsername());
                bookMarkedMap.put(bookMarked.getNews().getId(),bookMarked);
                bookMarkedNews.put(bookMarked.getLogin().getUsername(),bookMarkedMap);
            }
        }
            CacheBasicPutRequest<Map<String,Map<Long,BookMarked>>> cacheBasicPutRequest = new CacheBasicPutRequest<>();
            cacheBasicPutRequest.setNamespace(BOOKMARK_NAMESPACE);
            cacheBasicPutRequest.setKey(Constants.BOOKMARK_KEY);
            cacheBasicPutRequest.setTtl(ttlMap.getOrDefault(Constants.CACHE_ALL_LABELS_KEY,300));
            cacheBasicPutRequest.setUpdateTtl(true);
            cacheBasicPutRequest.setValue(bookMarkedNews);
            if(!CollectionUtils.isEmpty(bookMarkedNews)){
                cacheService.put(cacheBasicPutRequest);
            }
    }

    public Map<String,Map<Long, BookMarked>> getAllBookmarksOfUser() throws Exception {
        CacheRequest cacheRequest = new CacheRequest();
        cacheRequest.setNamespace(BOOKMARK_NAMESPACE);
        cacheRequest.setKey(Constants.BOOKMARK_KEY);
        Map<String,Map<Long, BookMarked>> bookMarkedMap;
        try {
            bookMarkedMap = cacheService.get(cacheRequest, new TypeReference<Map<String,Map<Long, BookMarked>>>() {});
            if(CollectionUtils.isEmpty(bookMarkedMap)){
                throw new Exception();
            }
            return bookMarkedMap;
        }catch (Exception e){
            LOGGER.error("BookMarkComponent.getAllBookMarksOfUser:  Could not fetch bookmark mappings from cache",e);
        }
        initialiseBookmarkCache();
        return bookMarkedNews;
    }

    public void onlyPutAllBookmarks(Map<String,Map<Long, BookMarked>> onlyAddBookmark) throws Exception {
        CacheBasicPutRequest<Map<String,Map<Long, BookMarked>>> cacheBasicPutRequest = new CacheBasicPutRequest<>();
        cacheBasicPutRequest.setNamespace(BOOKMARK_NAMESPACE);
        cacheBasicPutRequest.setKey(Constants.BOOKMARK_KEY);
        cacheBasicPutRequest.setTtl(ttlMap.getOrDefault(Constants.CACHE_ALL_LABELS_KEY,300));
        cacheBasicPutRequest.setUpdateTtl(true);
        cacheBasicPutRequest.setValue(onlyAddBookmark);
        if(!CollectionUtils.isEmpty(onlyAddBookmark)){
            cacheService.put(cacheBasicPutRequest);
        }
    }

}
