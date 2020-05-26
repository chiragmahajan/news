package com.chirag.news.service;

public interface LoginService {
    String findUser(String username,String password);
    String saveUser(String username,String password);
    Boolean updateUser(String username,String password);
}
