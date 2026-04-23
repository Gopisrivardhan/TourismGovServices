package com.tourismgov.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    // This MUST exactly match the string used in your User Service
    private static final String SECRET = "bAW23OfGJzdzDw6XmZrCvxUrAWe1DjhOLvYUY7jMDqT";

    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    public Claims getClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        // User Service uses .getBytes(), so the Gateway MUST use .getBytes()
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }
}