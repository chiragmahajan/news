package com.chirag.news.repository;

import com.chirag.news.model.entity.Login;

public interface LoginRepository {
    void saveUser(Login login);
    Login findUser(String username,String password);
    void updateUser(Login login);
}
