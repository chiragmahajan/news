package com.chirag.news.model.DTO;

import lombok.Data;

@Data
public class NewsDTO {
    Long id;
    String news;
    Long likeCount;
    Integer isLiked;
    Integer isBookmarked;
    String author;
}
