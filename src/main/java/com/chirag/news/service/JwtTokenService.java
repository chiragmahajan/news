package com.chirag.news.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.chirag.news.config.appConfig.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenService {

    @Autowired
    private JwtConfig jwtConfig;

    public String generateToken(){
        try {
            return JWT.create().withIssuer("Chirag").withClaim("client-id", jwtConfig.getClientId())
                    .withIssuedAt(new Date())
                    .sign(Algorithm.HMAC256(jwtConfig.getSecretKey()));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean verifyS2sJwtToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtConfig.getSecretKey()))
                    .withClaim("client-id", jwtConfig.getClientId())
                    .withIssuer("Chirag").acceptIssuedAt(jwtConfig.getLeewayWindowIssuedAt()).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            Date issuedDate = decodedJWT.getIssuedAt();
            Date currentDate = new Date();
            return issuedDate.getTime() > currentDate.getTime() - jwtConfig.getExpiryTime();
        } catch (Exception e) {
            return false;
        }
    }
}
