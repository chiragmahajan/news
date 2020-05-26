package com.chirag.news.repository;

import com.chirag.news.model.DTO.NewsDTO;
import com.chirag.news.model.entity.News;

import java.util.List;

public interface MyHomeNewsRepository {
    List<News> getNews(String username);
    void updateNews(Long id,String newsBody);
    void addNews(String username, String newsBody);
    void deleteNews(Long id);
    List<News> getAllNews();
}
