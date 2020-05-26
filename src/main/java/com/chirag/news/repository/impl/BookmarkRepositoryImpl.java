package com.chirag.news.repository.impl;

import com.chirag.news.constants.Constants;
import com.chirag.news.model.entity.BookMarked;
import com.chirag.news.repository.BookmarkRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class BookmarkRepositoryImpl implements BookmarkRepository {

    @PersistenceContext(unitName = Constants.MASTER_PERSISTENCE_UNIT_NAME)
    private EntityManager masterEntityManager;

    @PersistenceContext(unitName = Constants.SLAVE_PERSISTENCE_UNIT_NAME)
    private EntityManager slaveEntityManager;

    @Override
    public List<BookMarked> getAll() {
        Query query = slaveEntityManager.createNativeQuery("select * from bookmarks where is_bookmarked=1",BookMarked.class);
        return query.getResultList();
    }

    @Override
    @Transactional(transactionManager = Constants.MASTER_TRANSACTION_MANAGER)
    public void addBookMark(String username,Long newsId,Integer isBookmark) {
        Query query = masterEntityManager.createNativeQuery("insert into bookmarks(id,username) values(:newsId,:username) ON DUPLICATE KEY UPDATE is_bookmarked=:isBookmark",BookMarked.class);
        query.setParameter("newsId",newsId);
        query.setParameter("username",username);
        query.setParameter("isBookmark",isBookmark);
        query.executeUpdate();
    }
}
