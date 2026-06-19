package com.finrag4j.auth.service;

import com.finrag4j.auth.entity.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT服务
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private final RedisTemplate<String, String> redisTemplate;

    public JwtService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成Token
     */
    public String generateToken(SysUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成Token对（AccessToken + RefreshToken）
     */
    public Map<String, Object> generateTokens(SysUser user) {
        String accessToken = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("userId", user.getId());
        tokens.put("username", user.getUsername());

        return tokens;
    }

    /**
     * 生成RefreshToken
     */
    private String generateRefreshToken(SysUser user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 7); // 7天

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            // 检查是否在黑名单
            return !isBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将Token加入黑名单
     */
    public void blacklistToken(String token) {
        String key = "jwt:blacklist:" + token;
        redisTemplate.opsForValue().set(key, "1", expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查Token是否在黑名单
     */
    private boolean isBlacklisted(String token) {
        String key = "jwt:blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 刷新Token
     */
    public Map<String, Object> refreshTokens(String refreshToken) {
        Claims claims = parseToken(refreshToken);

        if (!"refresh".equals(claims.get("type"))) {
            throw new RuntimeException("Invalid refresh token");
        }

        Long userId = Long.parseLong(claims.getSubject());
        // TODO: 从数据库或缓存获取用户信息

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", generateTokenByUserId(userId));
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    private String generateTokenByUserId(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
