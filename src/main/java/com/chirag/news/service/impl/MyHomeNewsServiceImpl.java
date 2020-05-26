package com.chirag.news.service.impl;

import com.chirag.news.component.BookMarkComponent;
import com.chirag.news.component.LikeComponent;
import com.chirag.news.component.NewsComponent;
import com.chirag.news.config.appConfig.AppConfig;
import com.chirag.news.constants.Constants;
import com.chirag.news.model.DTO.NewsDTO;
import com.chirag.news.model.entity.BookMarked;
import com.chirag.news.model.entity.Likes;
import com.chirag.news.model.entity.News;
import com.chirag.news.repository.BookmarkRepository;
import com.chirag.news.repository.LikeRepository;
import com.chirag.news.repository.MyHomeNewsRepository;
import com.chirag.news.service.MyHomeNewsService;
import com.chirag.news.util.EncryptionUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MyHomeNewsServiceImpl extends EncryptionUtil  implements MyHomeNewsService {

    private static final Logger LOG = LoggerFactory.getLogger(MyHomeNewsServiceImpl.class);

    @Autowired
    private MyHomeNewsRepository myHomeNewsRepository;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private BookmarkRepository bookmarkRepository;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private BookMarkComponent bookMarkComponent;
    @Autowired
    private NewsComponent newsComponent;
    @Autowired
    private LikeComponent likeComponent;

    @Override
//    @Cacheable(value = Constants.USER_NEWS_CACHE, cacheManager = Constants.CAFFEINE_CACHE_MANAGER, key = "#username", sync = true)
    public List<NewsDTO> getNews(String username) throws Exception {

        List<NewsDTO> newsOfUser = new ArrayList<>();

        Map<Long,Likes> likesOfUser = new HashMap<>();
        Map<Long,BookMarked> bookmarksOfUser = new HashMap<>();
        Map<String,List<Likes>> allLikesOfUser = likeComponent.getAllLikesOfUser();
        Map<String,List<BookMarked>> allBookMarksOfUser = bookMarkComponent.getAllBookmarksOfUser();
        Map<String,List<News>> userNews = newsComponent.getUserNews();
        Map<Long,Long> totalLikes = likeComponent.getTotalLikesOnNews();

        String encryptedUser = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
        if(!CollectionUtils.isEmpty(allLikesOfUser)){
            List<Likes> likesList = allLikesOfUser.get(encryptedUser);
            if(!CollectionUtils.isEmpty(likesList)){
                for(Likes likes: likesList) {
                   likesOfUser.put(likes.getNews().getId(),likes);
                }
            }
        }
        if(!CollectionUtils.isEmpty(allBookMarksOfUser)){
            List<BookMarked> bookMarkedList = allBookMarksOfUser.get(encryptedUser);
            if(!CollectionUtils.isEmpty(bookMarkedList)){
                for(BookMarked bookMarked: bookMarkedList) {
                    bookmarksOfUser.put(bookMarked.getNews().getId(),bookMarked);
                }
            }
        }
        if(!CollectionUtils.isEmpty(userNews)){
            List<News> newsList = userNews.get(encryptedUser);
            if(!CollectionUtils.isEmpty(newsList)){
                for(News news: newsList) {
                    NewsDTO newsDTO = new NewsDTO();
                    newsDTO.setId(news.getId());
                    if(!CollectionUtils.isEmpty(totalLikes)){
                        newsDTO.setLikeCount(totalLikes.get(news.getId()));
                    }
                    newsDTO.setNews(news.getNewsBody());
                    newsDTO.setAuthor(username);
                    if(!CollectionUtils.isEmpty(likesOfUser)){
                        if(likesOfUser.get(news.getId())==null){
                            newsDTO.setIsLiked(0);
                        }else{
                            newsDTO.setIsLiked(1);
                        }
                    }
                    if(!CollectionUtils.isEmpty(bookmarksOfUser)){
                        if(bookmarksOfUser.get(news.getId())==null){
                            newsDTO.setIsBookmarked(0);
                        }else{
                            newsDTO.setIsBookmarked(1);
                        }
                    }
                    newsOfUser.add(newsDTO);
                }
                return newsOfUser;
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
//    @CacheEvict(value = Constants.USER_NEWS_CACHE, cacheManager = Constants.CAFFEINE_CACHE_MANAGER, key = "#username")
    public ResponseEntity updateNews(Long id, String newsBody,String username) throws Exception {
        try {
            myHomeNewsRepository.updateNews(id, newsBody);
            return new ResponseEntity("news updated", HttpStatus.OK);
        }catch (Exception e){
            LOG.error("news not updated with id : {}",id);
            throw new Exception("unable to update news");
        }

    }

    @Override
//    @CacheEvict(value = Constants.USER_NEWS_CACHE, cacheManager = Constants.CAFFEINE_CACHE_MANAGER, key = "#username")
    public ResponseEntity addNews(String username, String newsBody) throws Exception {
        try{
            String userEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(), username);
            int id = myHomeNewsRepository.addNews(userEncrypt,newsBody);
            return new ResponseEntity("news added with id = "+id,HttpStatus.OK);
        }catch (Exception e){
            LOG.error("unable to add news");
            throw new Exception("unable to add news");
        }
    }

    @Override
//    @CacheEvict(value = Constants.USER_NEWS_CACHE, cacheManager = Constants.CAFFEINE_CACHE_MANAGER, key = "#username")
    public Boolean deleteNews(Long id,String username) {
        try {
            myHomeNewsRepository.deleteNews(id);
            return true;
        }catch (Exception e){
            LOG.error("news not deleted with id :{}",id);
        }
        return false;

    }

    @Override
    public List<NewsDTO> bookmarkedNews(String username) throws Exception {

        List<NewsDTO> bookmarkedNews = new ArrayList<>();
        Map<Long,Likes> userLikedMap = null;

        Map<String,List<BookMarked>> listMap = bookMarkComponent.getAllBookmarksOfUser();
        Map<Long,News> allNews = newsComponent.getAllNews();
        Map<String,List<Likes>> allLikes = likeComponent.getAllLikesOfUser();

        String encryptedUser = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);

        if(!CollectionUtils.isEmpty(allLikes)){
            List<Likes> userLiked = allLikes.get(encryptedUser);
            if(!CollectionUtils.isEmpty(userLiked)){
                for(Likes likes: userLiked){
                    userLikedMap.put(likes.getNews().getId(),likes);
                }
            }
        }
        Map<Long,Long> totalLikes = likeComponent.getTotalLikesOnNews();
        if(!CollectionUtils.isEmpty(listMap) && !CollectionUtils.isEmpty(allNews)){
            List<BookMarked> bookMarkedList = listMap.get(encryptedUser);
            if(!CollectionUtils.isEmpty(bookMarkedList)){
                for(BookMarked bookMarked: bookMarkedList){
                    NewsDTO newsDTO = new NewsDTO();
                    newsDTO.setId(bookMarked.getNews().getId());
                    if(!StringUtils.isEmpty(allNews.get(bookMarked.getNews().getId()).getLogin().getUsername())){
                        String decryptedAuthor = EncryptionUtil.decrypt(appConfig.getEncryptionKey(),allNews.get(bookMarked.getNews().getId()).getLogin().getUsername());
                        newsDTO.setAuthor(decryptedAuthor);
                    }
                    newsDTO.setNews(allNews.get(bookMarked.getNews().getId()).getNewsBody());
                    if(!CollectionUtils.isEmpty(totalLikes)){
                        newsDTO.setLikeCount(totalLikes.get(bookMarked.getNews().getId()));
                    }
                    if(!CollectionUtils.isEmpty(userLikedMap)){
                        if(userLikedMap.get(bookMarked.getNews().getId())==null){
                            newsDTO.setIsLiked(0);
                        }else{
                            newsDTO.setIsLiked(1);
                        }
                    }
                    newsDTO.setIsBookmarked(1);
                    bookmarkedNews.add(newsDTO);
                }
                return bookmarkedNews;
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<NewsDTO> likedNews(String username) throws Exception {

        List<NewsDTO> likedNews = new ArrayList<>();
        Map<Long,BookMarked> userBookmarkMap = null;

        Map<String,List<Likes>> listMap = likeComponent.getAllLikesOfUser();
        Map<Long,News> allNews = newsComponent.getAllNews();
        Map<String,List<BookMarked>> allBookmarkedNews = bookMarkComponent.getAllBookmarksOfUser();

        String encryptedUser = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
        if(!CollectionUtils.isEmpty(allBookmarkedNews)){
            List<BookMarked> bookMarkedList = allBookmarkedNews.get(encryptedUser);
            if(!CollectionUtils.isEmpty(bookMarkedList)){
                for(BookMarked bookMarked: bookMarkedList){
                    userBookmarkMap.put(bookMarked.getNews().getId(),bookMarked);
                }
            }
        }
        Map<Long,Long> totalLikes = likeComponent.getTotalLikesOnNews();
        if(!CollectionUtils.isEmpty(listMap) && !CollectionUtils.isEmpty(allNews)){
            List<Likes> likesList = listMap.get(encryptedUser);
            if(!CollectionUtils.isEmpty(likesList)){
                for(Likes likes: likesList){
                    NewsDTO newsDTO = new NewsDTO();
                    newsDTO.setId(likes.getNews().getId());
                    if(!StringUtils.isEmpty(allNews.get(likes.getNews().getId()).getLogin().getUsername())){
                        String decryptedAuthor = EncryptionUtil.decrypt(appConfig.getEncryptionKey(),allNews.get(likes.getNews().getId()).getLogin().getUsername());
                        newsDTO.setAuthor(decryptedAuthor);
                    }
                    newsDTO.setNews(allNews.get(likes.getNews().getId()).getNewsBody());
                    if(!CollectionUtils.isEmpty(totalLikes)){
                        newsDTO.setLikeCount(totalLikes.get(likes.getNews().getId()));
                    }
                    if(!CollectionUtils.isEmpty(userBookmarkMap)){
                        if(userBookmarkMap.get(likes.getNews().getId())==null){
                            newsDTO.setIsBookmarked(0);
                        }else{
                            newsDTO.setIsBookmarked(1);
                        }
                    }
                    newsDTO.setIsLiked(1);
                    likedNews.add(newsDTO);
                }
                return likedNews;
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Boolean likeNews(String username, Long newsId, Integer like) {
        try{
            String userEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
            likeRepository.addLike(userEncrypt, newsId, like);
            return true;
        }catch(Exception e){
            LOG.error("unable to add like");
        }
        return false;    }

    @Override
    public Boolean bookmarkNews(String username, Long newsId, Integer bookmark) {
        try{
            String userEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
           bookmarkRepository.addBookMark(userEncrypt, newsId, bookmark);

           return true;
        }catch(Exception e){
            LOG.error("unable to add bookmark");
        }
        return false;
    }
}
