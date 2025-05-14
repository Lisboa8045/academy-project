package com.academy.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

import static java.security.KeyRep.Type.SECRET;

@Component
public class JwtUtil {

    private final String SECRET_KEY;

    public JwtUtil(@Value("${secret.key}") String secretKey) {
        this.SECRET_KEY = secretKey;
    }

    public String getSecretKey() {
        return SECRET_KEY;
    }
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, String.valueOf(SECRET))
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(String.valueOf(SECRET))
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername());
    }
}
