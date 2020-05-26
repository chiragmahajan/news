package com.chirag.news.repository.impl;

import com.chirag.news.constants.Constants;
import com.chirag.news.model.entity.Login;
import com.chirag.news.repository.LoginRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class LoginRepositoryImpl implements LoginRepository {

    @PersistenceContext(unitName = Constants.MASTER_PERSISTENCE_UNIT_NAME)
    private EntityManager masterEntityManager;

    @PersistenceContext(unitName = Constants.SLAVE_PERSISTENCE_UNIT_NAME)
    private EntityManager slaveEntityManager;

    @Override
    @Transactional(transactionManager = Constants.MASTER_TRANSACTION_MANAGER)
    public void saveUser(Login login) {
       masterEntityManager.persist(login);
    }

    @Override
    public Login findUser(String username, String password) {
        Query query = slaveEntityManager.createNativeQuery("select * from login where username =:username and password =:password",Login.class);
        query.setParameter("username",username);
        query.setParameter("password",password);
        return (Login) query.getSingleResult();
    }

    @Override
    @Transactional(transactionManager = Constants.MASTER_TRANSACTION_MANAGER)
    public void updateUser(Login login) {
        masterEntityManager.merge(login);
    }
}
