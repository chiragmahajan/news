package com.chirag.news.service;

import com.chirag.news.model.DataExchange;

public interface LoginService {
    DataExchange findUser(String username, String password);
    DataExchange saveUser(String username,String password);
    DataExchange updateUser(String username,String password);
}
