package com.chirag.news.repository;

import com.chirag.news.model.entity.Likes;

import java.util.List;

public interface LikeRepository {
    List<Likes> getAllLikes();
    void addLike(String username,Long newsId,Integer isLiked);
}
