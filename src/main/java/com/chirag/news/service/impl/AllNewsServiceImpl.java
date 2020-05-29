package com.chirag.news.service.impl;

import com.chirag.news.component.BookMarkComponent;
import com.chirag.news.component.LikeComponent;
import com.chirag.news.component.NewsComponent;
import com.chirag.news.config.appConfig.AppConfig;
import com.chirag.news.model.DTO.NewsDTO;
import com.chirag.news.model.entity.BookMarked;
import com.chirag.news.model.entity.Likes;
import com.chirag.news.model.entity.News;
import com.chirag.news.service.AllNewsService;
import com.chirag.news.util.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AllNewsServiceImpl extends EncryptionUtil implements AllNewsService  {

    private static final Logger LOGGER = LogManager.getLogger(AllNewsServiceImpl.class);

    @Autowired
    private NewsComponent newsComponent;
    @Autowired
    private LikeComponent likeComponent;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private BookMarkComponent bookMarkComponent;

    @Override
    public List<NewsDTO> allNews(int pageNo,Boolean login) throws Exception {
        List<NewsDTO> newsDTOList = new ArrayList<>();
        Map<Long,Long> getLikesCount = null;
        List<News> newsList = null;
        try {
            newsList = newsComponent.getAllNewsPerPage(pageNo);
        }catch (Exception e){
            LOGGER.error("unable to fetch news");
            return Collections.EMPTY_LIST;
        }
        try {
           getLikesCount =  likeComponent.getTotalLikesOnNews();
        }catch(Exception e){
            LOGGER.error("unable to fetch likes");
        }
        if(login==false){
            if(CollectionUtils.isEmpty(newsList)){
                return Collections.EMPTY_LIST;
            }
            for(News news:newsList){
                NewsDTO newsDTO = new NewsDTO();
                newsDTO.setId(news.getId());
                newsDTO.setIsLiked(0);
                newsDTO.setIsBookmarked(0);
                String userDecrypt = EncryptionUtil.decrypt(appConfig.getEncryptionKey(),news.getLogin().getUsername());
                newsDTO.setAuthor(userDecrypt);
                if(!CollectionUtils.isEmpty(getLikesCount)){
                    newsDTO.setLikeCount(getLikesCount.get(news.getId()));
                }
                newsDTO.setNews(news.getNewsBody());
                newsDTOList.add(newsDTO);
            }
        }else{
            if(CollectionUtils.isEmpty(newsList)){
                return Collections.EMPTY_LIST;
            }

            Map<String,Map<Long,Likes>> allLikesOfUser = null;
            Map<String,Map<Long,BookMarked>> allBookMarksOfUser = null;

            try {
                allLikesOfUser = likeComponent.getAllLikesOfUser();
            }catch(Exception e){
                LOGGER.error("unable to fetch user likes");;
            }
            try {
                allBookMarksOfUser = bookMarkComponent.getAllBookmarksOfUser();
            }catch (Exception e){
                LOGGER.error("unable to fetch user bookmarks");
            }
            for(News news: newsList){
                NewsDTO newsDTO = new NewsDTO();
                newsDTO.setId(news.getId());
                newsDTO.setNews(news.getNewsBody());
                String userDecrypt = EncryptionUtil.decrypt(appConfig.getEncryptionKey(),news.getLogin().getUsername());
                newsDTO.setAuthor(userDecrypt);
                if(!CollectionUtils.isEmpty(getLikesCount)){
                    newsDTO.setLikeCount(getLikesCount.get(news.getId()));
                }
                if(!CollectionUtils.isEmpty(allLikesOfUser)){
                    Map<Long, Likes> likesOfUser = allLikesOfUser.get(news.getLogin().getUsername());
                    if(!CollectionUtils.isEmpty(likesOfUser)){
                        if(likesOfUser.get(news.getId())==null){
                            newsDTO.setIsLiked(0);
                        }else{
                            newsDTO.setIsLiked(1);
                        }
                    }
                }
                if(!CollectionUtils.isEmpty(allBookMarksOfUser)){
                    Map<Long, BookMarked> bookmarksOfUser = allBookMarksOfUser.get(news.getLogin().getUsername());
                    if(!CollectionUtils.isEmpty(bookmarksOfUser)){
                        if(bookmarksOfUser.get(news.getId())==null){
                            newsDTO.setIsBookmarked(0);
                        }else{
                            newsDTO.setIsBookmarked(1);
                        }
                    }
                }
                newsDTOList.add(newsDTO);
            }
        }
        return newsDTOList;
    }
}
