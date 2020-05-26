package com.chirag.news.service.impl;

import com.chirag.news.component.BookMarkComponent;
import com.chirag.news.component.LikeComponent;
import com.chirag.news.component.NewsComponent;
import com.chirag.news.config.appConfig.AppConfig;
import com.chirag.news.constants.Constants;
import com.chirag.news.model.DTO.NewsDTO;
import com.chirag.news.model.entity.BookMarked;
import com.chirag.news.model.entity.Likes;
import com.chirag.news.model.entity.Login;
import com.chirag.news.model.entity.News;
import com.chirag.news.repository.BookmarkRepository;
import com.chirag.news.repository.LikeRepository;
import com.chirag.news.repository.MyHomeNewsRepository;
import com.chirag.news.service.MyHomeNewsService;
import com.chirag.news.util.EncryptionUtil;
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
import java.util.stream.Collectors;

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
        Map<String,Map<Long,Likes>> allLikesOfUser = likeComponent.getAllLikesOfUser();
        Map<String,Map<Long,BookMarked>> allBookMarksOfUser = bookMarkComponent.getAllBookmarksOfUser();
        Map<String,Map<Long,News>> userNews = newsComponent.getUserNews();
        Map<Long,Long> totalLikes = likeComponent.getTotalLikesOnNews();

        String encryptedUser = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
        if(!CollectionUtils.isEmpty(allLikesOfUser)){
            likesOfUser = allLikesOfUser.get(encryptedUser);
        }
        if(!CollectionUtils.isEmpty(allBookMarksOfUser)){
            bookmarksOfUser = allBookMarksOfUser.get(encryptedUser);
        }
        if(!CollectionUtils.isEmpty(userNews)){
            Map<Long,News> newsMap = userNews.get(encryptedUser);
            List<News> newsList = null;
            if(!CollectionUtils.isEmpty(newsMap)){
                newsList = newsMap.values().stream().collect(Collectors.toList());;
            }
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
            Map<Long,News> allNews = newsComponent.getAllNews();
            if(!CollectionUtils.isEmpty(allNews)) {
                allNews.get(id).setNewsBody(newsBody);
                newsComponent.onlyPutAllNews(allNews);
            }
            String usernameEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
            Map<String,Map<Long,News>> userNewsString = newsComponent.getUserNews();
            if(!CollectionUtils.isEmpty(userNewsString)){
                Map<Long,News> userNews = userNewsString.get(usernameEncrypt);
                if(!CollectionUtils.isEmpty(userNews)){
                    userNews.get(id).setNewsBody(newsBody);
                    newsComponent.onlyPutUserNews(userNewsString);
                }
            }
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
            myHomeNewsRepository.addNews(userEncrypt,newsBody);
            return new ResponseEntity("news added",HttpStatus.OK);
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
            Map<Long,News> allNews = newsComponent.getAllNews();
            if(!CollectionUtils.isEmpty(allNews)) {
                allNews.remove(id);
                newsComponent.onlyPutAllNews(allNews);
            }
            String usernameEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
            Map<String,Map<Long,News>> userNewsString = newsComponent.getUserNews();
            if(!CollectionUtils.isEmpty(userNewsString)){
                Map<Long,News> userNews = userNewsString.get(usernameEncrypt);
                if(!CollectionUtils.isEmpty(userNews)){
                    userNews.remove(id);
                    newsComponent.onlyPutUserNews(userNewsString);
                }
            }
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

        Map<String,Map<Long,BookMarked>> listMap = bookMarkComponent.getAllBookmarksOfUser();
        Map<Long,News> allNews = newsComponent.getAllNews();
        Map<String,Map<Long,Likes>> allLikes = likeComponent.getAllLikesOfUser();

        String encryptedUser = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);

        if(!CollectionUtils.isEmpty(allLikes)){
            userLikedMap = allLikes.get(encryptedUser);
        }
        Map<Long,Long> totalLikes = likeComponent.getTotalLikesOnNews();
        if(!CollectionUtils.isEmpty(listMap) && !CollectionUtils.isEmpty(allNews)){
            Map<Long,BookMarked> bookMarkedMap = listMap.get(encryptedUser);
            List<BookMarked> bookMarkedList = null;
            if(!CollectionUtils.isEmpty(bookMarkedMap)){
                bookMarkedList = bookMarkedMap.values().stream().collect(Collectors.toList());;
            }
            if(!CollectionUtils.isEmpty(bookMarkedList)){
                for(BookMarked bookMarked: bookMarkedList){
                    if(allNews.get(bookMarked.getNews().getId())==null){
                        continue;
                    }
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

        Map<String,Map<Long,Likes>> listMap = likeComponent.getAllLikesOfUser();
        Map<Long,News> allNews = newsComponent.getAllNews();
        Map<String,Map<Long,BookMarked>> allBookmarkedNews = bookMarkComponent.getAllBookmarksOfUser();

        String encryptedUser = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
        if(!CollectionUtils.isEmpty(allBookmarkedNews)){
            userBookmarkMap = allBookmarkedNews.get(encryptedUser);
        }
        Map<Long,Long> totalLikes = likeComponent.getTotalLikesOnNews();
        if(!CollectionUtils.isEmpty(listMap) && !CollectionUtils.isEmpty(allNews)){
            Map<Long,Likes> likesMap = listMap.get(encryptedUser);
            List<Likes> likesList = null;
            if(!CollectionUtils.isEmpty(likesMap)){
                if(!CollectionUtils.isEmpty(likesMap)){
                    likesList = likesMap.values().stream().collect(Collectors.toList());;
                }
                for(Likes likes: likesList){
                    if(allNews.get(likes.getNews().getId())==null){
                        continue;
                    }
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
    public Long likeNews(String username, Long newsId, Integer like) {
        Long value = 0L;
        try{
            String userEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
            likeRepository.addLike(userEncrypt, newsId, like);
            Map<Long,Long> likeCount = likeComponent.getTotalLikesOnNews();
            value = likeCount.get(newsId);
            if(like==0){
                if(!CollectionUtils.isEmpty(likeCount)){
                    if(likeCount.get(newsId)!=null) {
                        likeCount.put(newsId, likeCount.get(newsId) - 1);
                    }
                }
            }else{
                if(!CollectionUtils.isEmpty(likeCount)){
                    if(likeCount.get(newsId)!=null) {
                        likeCount.put(newsId, likeCount.get(newsId) + 1);
                    }
                }
            }
            likeComponent.onlyAddLikeCount(likeCount);
            Map<String,Map<Long,Likes>> likedMapString = likeComponent.getAllLikesOfUser();
            if(like==0){
                if(!CollectionUtils.isEmpty(likedMapString)){
                    Map<Long,Likes> likesMap = likedMapString.get(userEncrypt);
                    if(!CollectionUtils.isEmpty(likesMap)){
                        likesMap.remove(newsId);
                        likeComponent.onlyAddUserLikedNews(likedMapString);
                    }
                }
            }else{
                News news = null;
                if(!CollectionUtils.isEmpty(newsComponent.getAllNews())){
                    news = newsComponent.getAllNews().get(newsId);
                }
                Likes likes = new Likes();
                likes.setIsLiked(true);
                likes.setNews(news);
                Login login = new Login();
                login.setUsername(username);
                likes.setLogin(login);
                if(!CollectionUtils.isEmpty(likedMapString)){
                    Map<Long,Likes> likesMap = likedMapString.get(userEncrypt);
                    if(CollectionUtils.isEmpty(likesMap)){
                        likesMap = new HashMap<>();
                    }
                    likesMap.put(newsId,likes);
                    likedMapString.put(username,likesMap);
                }else{
                    likedMapString = new HashMap<>();
                    Map<Long,Likes> likesMap = new HashMap<>();
                    likesMap.put(newsId,likes);
                    likedMapString.put(username,likesMap);
                }
                likeComponent.onlyAddUserLikedNews(likedMapString);
            }
            return likeCount.get(newsId);
        }catch(Exception e){
            LOG.error("unable to add like");
        }
        return value;
    }

    @Override
    public Boolean bookmarkNews(String username, Long newsId, Integer bookmark) {
        try{
            String userEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),username);
            bookmarkRepository.addBookMark(userEncrypt, newsId, bookmark);
            Map<String,Map<Long,BookMarked>> bookmarkedMapString = bookMarkComponent.getAllBookmarksOfUser();
            if(bookmark==0){
                if(!CollectionUtils.isEmpty(bookmarkedMapString)){
                    Map<Long,BookMarked> bookMarkedMap = bookmarkedMapString.get(userEncrypt);
                    if(!CollectionUtils.isEmpty(bookMarkedMap)){
                        bookMarkedMap.remove(newsId);
                        bookMarkComponent.onlyPutAllBookmarks(bookmarkedMapString);
                    }
                }
            }else{
                News news = null;
                if(!CollectionUtils.isEmpty(newsComponent.getAllNews())){
                    news = newsComponent.getAllNews().get(newsId);
                }
                BookMarked bookMarked = new BookMarked();
                bookMarked.setIsBookmarked(true);
                bookMarked.setNews(news);
                Login login = new Login();
                login.setUsername(username);
                bookMarked.setLogin(login);
                if(!CollectionUtils.isEmpty(bookmarkedMapString)){
                    Map<Long,BookMarked> bookMarkedMap = bookmarkedMapString.get(userEncrypt);
                    if(CollectionUtils.isEmpty(bookMarkedMap)){
                        bookMarkedMap = new HashMap<>();
                    }
                    bookMarkedMap.put(newsId,bookMarked);
                    bookmarkedMapString.put(username,bookMarkedMap);
                }else{
                    bookmarkedMapString = new HashMap<>();
                    Map<Long,BookMarked> bookMarkedMap = new HashMap<>();
                    bookMarkedMap.put(newsId,bookMarked);
                    bookmarkedMapString.put(username,bookMarkedMap);
                }
                bookMarkComponent.onlyPutAllBookmarks(bookmarkedMapString);
            }
           return true;
        }catch(Exception e){
            LOG.error("unable to add bookmark");
        }
        return false;
    }
}
