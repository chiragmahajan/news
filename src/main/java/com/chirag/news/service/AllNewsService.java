package com.chirag.news.service;

import com.chirag.news.model.DTO.NewsDTO;

import java.util.List;

public interface AllNewsService {
    List<NewsDTO> allNews(int pageNo,Boolean login) throws Exception;
}
