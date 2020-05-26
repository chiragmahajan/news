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
            Map<String, String> claimsMap = new HashMap<>();
            long timeStamp = System.currentTimeMillis();
            claimsMap.put("client-id", jwtConfig.getClientId());
            claimsMap.put("ts", Long.toString(timeStamp));
            JWTCreator.Builder jwtBuilder = JWT.create().withIssuer("Chirag").withIssuedAt(new Date());
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            jwtBuilder.withHeader(header);
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecretKey());

            return jwtBuilder.sign(algorithm);
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
