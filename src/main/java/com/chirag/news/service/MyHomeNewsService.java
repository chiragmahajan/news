package com.chirag.news.service;

import com.chirag.news.model.DTO.NewsDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MyHomeNewsService {
    List<NewsDTO> getNews(String username) throws Exception;
    ResponseEntity updateNews(Long id, String newsBody,String username) throws Exception;
    ResponseEntity addNews(String username, String newsBody) throws Exception;
    Boolean deleteNews(Long id,String username);
    List<NewsDTO> bookmarkedNews(String username) throws Exception;
    List<NewsDTO> likedNews(String username) throws Exception;
    Long likeNews(String username,Long newsId,Integer like);
    Boolean bookmarkNews(String username,Long newsId,Integer bookmark);
}
