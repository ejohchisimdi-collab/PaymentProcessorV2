package com.chisimdi.webhook.service.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
@Service
public class JwtUtilService {
    @Value("${jwt.secret}")
    private String key;
    @Value("${jwt.expiration}")
    private int expiration;

    public SecretKey secretKey(){
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));

    }
    public String generateToken(String userName,int userId,String role){
        return Jwts.builder().subject(userName).
                claim("userId",userId).
                claim("role",role).
                signWith(secretKey()).
                issuedAt(new Date()).
                expiration(new Date(System.currentTimeMillis()+expiration)).compact();

    }
    public Claims extractAllClaims(String token){
        return Jwts.parser().verifyWith(secretKey()).build().parseSignedClaims(token).getPayload();
    }
    public Boolean isTokenValid(String token){
        try {
            extractAllClaims(token);
            return !extractAllClaims(token).getExpiration().before(new Date());
        }
        catch (Exception e){
            return false;
        }
    }
    public String extractUserName(String token){
        return extractAllClaims(token).getSubject();
    }
    public int extractUserId(String token){
        return extractAllClaims(token).get("userId",Integer.class);
    }
    public String extractRole(String token){
        return extractAllClaims(token).get("role",String.class);
    }

}
