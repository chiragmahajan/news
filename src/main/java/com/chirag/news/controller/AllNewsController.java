package com.chirag.news.controller;

import com.chirag.news.model.DTO.NewsDTO;
import com.chirag.news.service.AllNewsService;
import com.chirag.news.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class AllNewsController {

    @Autowired
    private AllNewsService allNewsService;
    @Autowired
    private JwtTokenService jwtTokenService;

    @ResponseBody
    @GetMapping("/news/{pageNo}")
    public List<NewsDTO> allNews(@PathVariable("pageNo") int pageNo,
                                 @RequestHeader(value = "x-jwt-token",required = false) String jwt) throws Exception {
        Boolean login = false;
        if(jwtTokenService.verifyS2sJwtToken(jwt)==true) {
            login = true;
        }
        return allNewsService.allNews(pageNo,login);
    }
}
