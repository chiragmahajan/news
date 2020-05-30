package com.chirag.news.repository.impl;

import com.chirag.news.constants.Constants;
import com.chirag.news.model.entity.News;
import com.chirag.news.repository.MyHomeNewsRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class MyNewsNewsRepositoryImpl implements MyHomeNewsRepository {

    @PersistenceContext(unitName = Constants.MASTER_PERSISTENCE_UNIT_NAME)
    private EntityManager masterEntityManager;

    @PersistenceContext(unitName = Constants.SLAVE_PERSISTENCE_UNIT_NAME)
    private EntityManager slaveEntityManager;

    @Override
    public List<News> getNews(String username) {
        Query query = slaveEntityManager.createNativeQuery("select * from news where username =:username and active=1", News.class);
        query.setParameter("username",username);
        return (List<News>) query.getResultList();
    }

    @Override
    @Transactional(transactionManager = Constants.MASTER_TRANSACTION_MANAGER)
    public void updateNews(Long id, String newsBody,String userEncrypt) {
        Query query = masterEntityManager.createNativeQuery("update news set news_body =:newsBody where id=:id and username=:username",News.class);
        query.setParameter("newsBody",newsBody);
        query.setParameter("username",userEncrypt);
        query.setParameter("id",id);
        query.executeUpdate();
    }

    @Override
    @Transactional(transactionManager = Constants.MASTER_TRANSACTION_MANAGER)
    public void addNews(String username, String newsBody) {
        Query query = masterEntityManager.createNativeQuery("insert into news(news_body,username) values(?,?)");
        query.setParameter(1,newsBody);
        query.setParameter(2,username);
        query.executeUpdate();
    }

    @Override
    @Transactional(transactionManager = Constants.MASTER_TRANSACTION_MANAGER)
    public void deleteNews(Long id) {
        Query query = masterEntityManager.createNativeQuery("update news set active=0 where id=:id",News.class);
        query.setParameter("id",id);
        query.executeUpdate();
    }

    @Override
    public List<News> getAllNews() {
        Query query = slaveEntityManager.createNativeQuery("select * from news where active=1 order by created_at desc", News.class);
        return query.getResultList();
    }

    @Override
    public News getNewlyAddedNews(String username, String newsBody) {
        Query query = slaveEntityManager.createNativeQuery("select * from news where username =:username and news_body =:newsBody and active=1 order by created_at desc  limit 1", News.class);
        query.setParameter("username",username);
        query.setParameter("newsBody",newsBody);
        List<News> newsList = query.getResultList();
        if(!CollectionUtils.isEmpty(newsList)){
            return newsList.get(0);
        }
        return null;
    }
}
