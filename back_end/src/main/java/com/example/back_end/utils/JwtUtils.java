package com.example.back_end.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);;
    private final long expireTime = 24 * 60 * 60 * 1000;

    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(username)
//                .claim("userId", userId)
//                .claim("username", username)
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey)
                .compact();
    }

    public String parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
