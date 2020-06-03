package com.chirag.news.controller;

import com.chirag.news.model.DTO.NewsDTO;
import com.chirag.news.service.JwtTokenService;
import com.chirag.news.service.MyHomeNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class MyNewsHomeController {

    @Autowired
    private MyHomeNewsService myHomeNewsService;
    @Autowired
    private JwtTokenService jwtTokenService;

    @ResponseBody
    @GetMapping("/home/my-news")
    public List<NewsDTO> getNews(@RequestHeader("username") String username,
                                 @RequestHeader("x-jwt-token") String jwt) throws Exception {
        if(jwtTokenService.verifyS2sJwtToken(jwt)!=true) {
            throw new Exception("jwt-token not authenticated");
        }
        return myHomeNewsService.getNews(username);
    }

    @ResponseBody
    @PostMapping("/home/my-news/edit/{id}")
    public ResponseEntity updateNews(@PathVariable("id") Long id,
                                     @RequestHeader("username") String username,
                                     @RequestParam("news_body") String newsBody,
                                     @RequestHeader("x-jwt-token") String jwt) throws Exception {
        if(jwtTokenService.verifyS2sJwtToken(jwt)!=true) {
            throw new Exception("jwt-token not authenticated");
        }
        return myHomeNewsService.updateNews(id,newsBody,username);
    }

    @ResponseBody
    @PostMapping("/home/my-news/add")
    public NewsDTO addNews(@RequestHeader("username") String username,
                           @RequestParam("news_body") String newsBody,
                           @RequestHeader("x-jwt-token") String jwt) throws Exception {
        if(jwtTokenService.verifyS2sJwtToken(jwt)!=true) {
            throw new Exception("jwt-token not authenticated");
        }
        return myHomeNewsService.addNews(username, newsBody);
    }

    @ResponseBody
    @PostMapping("/home/my-news/delete/{id}")
    public Boolean deleteNews(@PathVariable("id") Long id,
                              @RequestHeader("username") String username,
                              @RequestHeader("x-jwt-token") String jwt) throws Exception {
        if(jwtTokenService.verifyS2sJwtToken(jwt)!=true) {
            throw new Exception("jwt-token not authenticated");
        }
        return myHomeNewsService.deleteNews(id,username);
    }

    @ResponseBody
    @GetMapping("/home/my-news/bookmarked")
    public List<NewsDTO> bookmarkedNews(@RequestHeader("username")  String username,
                                        @RequestHeader("x-jwt-token") String jwt) throws Exception {
        if(jwtTokenService.verifyS2sJwtToken(jwt)!=true) {
            throw new Exception("jwt-token not authenticated");
        }
        return myHomeNewsService.bookmarkedNews(username);
    }

    @ResponseBody
    @GetMapping("/home/my-news/liked")
    public List<NewsDTO> likedNews(@RequestHeader("username") String username,
                                   @RequestHeader("x-jwt-token") String jwt) throws Exception {
        if(jwtTokenService.verifyS2sJwtToken(jwt)!=true) {
            throw new Exception("jwt-token not authenticated");
        }
        return myHomeNewsService.likedNews(username);
    }

    @ResponseBody
    @PostMapping("/home/my-news/bookmark-news/{id}")
    public Boolean bookmark(@RequestHeader("username") String username,
                            @PathVariable("id") Long id,
                            @RequestParam(value = "bookmark", required = false, defaultValue = "1") Integer bookmark,
                            @RequestHeader("x-jwt-token") String jwt) throws Exception {
        if(jwtTokenService.verifyS2sJwtToken(jwt)!=true) {
            throw new Exception("jwt-token not authenticated");
        }
        if(bookmark==1 || bookmark==0){
          return myHomeNewsService.bookmarkNews(username, id, bookmark);
        }else{
            throw new Exception("bookmarked value not(0/1)");
        }
    }

    @ResponseBody
    @PostMapping("/home/my-news/like-news/{id}")
    public Long like(@RequestHeader("username") String username,
                     @PathVariable("id") Long id,
                     @RequestParam(value = "like", required = false,defaultValue = "1") Integer like,
                     @RequestHeader("x-jwt-token") String jwt) throws Exception {
        if(jwtTokenService.verifyS2sJwtToken(jwt)!=true) {
            throw new Exception("jwt-token not authenticated");
        }
        if(like==1 || like==0){
            return myHomeNewsService.likeNews(username, id, like);
        }else{
            throw new Exception("like value not(0/1)");
        }
    }

}
