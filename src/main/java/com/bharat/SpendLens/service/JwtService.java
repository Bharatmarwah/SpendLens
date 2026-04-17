package com.bharat.SpendLens.service;

import com.bharat.SpendLens.entity.AuthUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private static final long ACCESS_TOKEN_VALIDITY = 3 * 60 * 1000L;
    private static final long REFRESH_TOKEN_VALIDITY = 30L * 24 * 60 * 60 * 1000L;

    private static final String SECRET_KEY = "myVerySecureSecretKeyFor256BitHmacSha256Algorithm123456";

    public String generateAccessToken(AuthUser user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("phone", user.getPhoneNumber())
                .claim("type", "access")   // 🔥 important
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(AuthUser user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(getKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractTokenType(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}