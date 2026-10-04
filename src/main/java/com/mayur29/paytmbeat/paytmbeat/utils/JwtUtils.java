package com.mayur29.paytmbeat.paytmbeat.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class JwtUtils {

    private String SECRET_KEY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456";

    public String generateToken(String username){
        Map<String, Objects> claims = new HashMap<>();
        return createToken(claims,username);
    }

    private String createToken(Map<String, Objects> claims, String username) {

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .header().empty().add("typ","JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000*60*5))
                .signWith(getSignKey())
                .compact();
    }

    public boolean validateToken(String token){
        System.out.println("extractClaims(token) ::"+extractClaims(token));
        return extractClaims(token).getExpiration().before(new java.util.Date());
    }

    public String extractUserName(String token){
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
