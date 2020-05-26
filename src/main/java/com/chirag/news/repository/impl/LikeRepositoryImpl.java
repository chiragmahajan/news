package com.chirag.news.repository.impl;

import com.chirag.news.constants.Constants;
import com.chirag.news.model.entity.Likes;
import com.chirag.news.repository.LikeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class LikeRepositoryImpl implements LikeRepository {

    @PersistenceContext(unitName = Constants.MASTER_PERSISTENCE_UNIT_NAME)
    private EntityManager masterEntityManager;

    @PersistenceContext(unitName = Constants.SLAVE_PERSISTENCE_UNIT_NAME)
    private EntityManager slaveEntityManager;

    @Override
    public List<Likes> getAllLikes() {
        Query query = slaveEntityManager.createNativeQuery("select * from likes where is_liked=1", Likes.class);
        return query.getResultList();
    }

    @Override
    @Transactional(transactionManager = Constants.MASTER_TRANSACTION_MANAGER)
    public void addLike(String username,Long newsId,Integer isLiked) {
        Query query = masterEntityManager.createNativeQuery("insert into likes(id,username) values(:newsId,:username) ON DUPLICATE KEY UPDATE is_liked=:isLiked",Likes.class);
        query.setParameter("newsId",newsId);
        query.setParameter("username",username);
        query.setParameter("isLiked",isLiked);
        query.executeUpdate();
    }

    @Override
    public List<Likes> getUserLikes(String username) {
        Query query = slaveEntityManager.createNativeQuery("select * from likes where is_liked=1 and username=:username", Likes.class);
        query.setParameter("username",username);
        return query.getResultList();
    }
}
