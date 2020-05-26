package com.chirag.news.repository;

import com.chirag.news.model.entity.BookMarked;

import java.util.List;

public interface BookmarkRepository {
    List<BookMarked> getAll();
    void addBookMark(String username,Long newsId,Integer isBookmark);
    List<BookMarked> getUserBookmarks(String username);
}
