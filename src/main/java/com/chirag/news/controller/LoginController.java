package com.chirag.news.controller;

import com.chirag.news.model.DataExchange;
import com.chirag.news.service.JwtTokenService;
import com.chirag.news.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private JwtTokenService jwtTokenService;

    @GetMapping(value = "/login")
    public Boolean login(@RequestHeader("x-jwt-token") String jwt) throws Exception {
        if(jwtTokenService.verifyS2sJwtToken(jwt)!=true) {
            throw new Exception("jwt-token not authenticated");
        }
        return false;
    }

    @ResponseBody
    @GetMapping(value = "/save-user")
    public DataExchange saveUser(@RequestParam("username") String username,
                            @RequestParam("password") String password) {
        return loginService.saveUser(username, password);
    }

    @ResponseBody
    @GetMapping(value = "/update-user")
    public DataExchange updateUser(@RequestParam("username") String username,
                              @RequestParam("password") String password) {
        return loginService.updateUser(username, password);
    }

    @ResponseBody
    @GetMapping(value = "/verify-user-details")
    public DataExchange verifyUserDetails(@RequestParam("username") String username,
                                          @RequestParam("password") String password) {
        return loginService.findUser(username,password);
    }


}
