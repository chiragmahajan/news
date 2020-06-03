package com.chirag.news.service.impl;

import com.chirag.news.config.appConfig.AppConfig;
import com.chirag.news.model.DataExchange;
import com.chirag.news.model.entity.Login;
import com.chirag.news.repository.LoginRepository;
import com.chirag.news.service.JwtTokenService;
import com.chirag.news.service.LoginService;
import com.chirag.news.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl extends EncryptionUtil implements LoginService {
    private static final Logger LOG = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    public DataExchange findUser(String username, String password) {
        String userEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(), username);
        String passwordEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(),password);
        try {
            Login findUser = loginRepository.findUser(userEncrypt, passwordEncrypt);
            if (findUser != null) {
                DataExchange dataExchange = new DataExchange();
                dataExchange.success("token",jwtTokenService.generateToken());
                return dataExchange;
            } else {
                LOG.error("unable to find user");
                return null;
            }
        }catch (Exception e){
            LOG.error("unable to find user");
            return null;
        }
    }

    @Override
    public DataExchange saveUser(String username, String password) {
        String userEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(), username);
        String passwordEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(), password);
        try {
            Login login = new Login();
            login.setPassword(passwordEncrypt);
            login.setUsername(userEncrypt);
            loginRepository.saveUser(login);
            DataExchange dataExchange = new DataExchange();
            dataExchange.success("token",jwtTokenService.generateToken());
            return dataExchange;
        } catch (Exception e) {
            LOG.error("unable to save user");
            return null;
        }
    }

    @Override
    public DataExchange updateUser(String username, String password) {
        String userEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(), username);
        String passwordEncrypt = EncryptionUtil.encrypt(appConfig.getEncryptionKey(), password);
        try {
            Login login = new Login();
            login.setPassword(passwordEncrypt);
            login.setUsername(userEncrypt);
            loginRepository.updateUser(login);
            DataExchange dataExchange = new DataExchange();
            dataExchange.success("updated",true);
            return dataExchange;
        } catch (Exception e) {
            LOG.error("unable to update user");
            DataExchange dataExchange = new DataExchange();
            dataExchange.success("updated",false);
            return dataExchange;
        }
    }
}
