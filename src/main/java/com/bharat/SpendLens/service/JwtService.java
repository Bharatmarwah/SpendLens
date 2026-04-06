package com.bharat.SpendLens.service;

import com.bharat.SpendLens.entity.AuthUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final long Access_Token_Validity = 60 * 3 * 1000L;
    private static final long Refresh_Token_Validity = 30L * 24 * 60 * 60 * 1000L;

    private static final String secretKey="bXlTdXBlclNlY3JldEtleUZvcktpdGZsaWstQXBpR2F0ZXdheTEyMzQ1Njc4OTA=";

    public String generateAccessToken(AuthUser user) {
        return Jwts
                .builder()
                .subject(user.getId().toString())
                .claim("phone",user.getPhoneNumber())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Access_Token_Validity))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(AuthUser user) {
        return Jwts
                .builder()
                .subject(user.getId().toString())
                .claim("phone",user.getPhoneNumber())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Refresh_Token_Validity))
                .signWith(getKey())
                .compact();
    }

    private Key getKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
