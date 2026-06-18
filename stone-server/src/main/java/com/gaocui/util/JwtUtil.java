package com.gaocui.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(@Value("${gaocui.jwt.secret:gaocui-jwt-secret-key-2026-min-32-chars!!}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 生成Token
    public String generate(Long merchantId, String email, boolean isVip) {
        return Jwts.builder()
            .claims(Map.of("merchantId", merchantId, "email", email, "isVip", isVip))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000L)) // 7天
            .signWith(key)
            .compact();
    }

    // 解析Token
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    // 校验Token是否有效
    public boolean validate(String token) {
        try { parse(token); return true; } catch (Exception e) { return false; }
    }
}
