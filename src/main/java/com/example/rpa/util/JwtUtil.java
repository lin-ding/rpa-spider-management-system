package com.example.rpa.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {
    @Value("${jwt.secret:RpaSpiderManagementSystemSecretKey2024ForLoginAuthentication}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey secretKey;
    
    @Autowired
    private TokenBlacklist tokenBlacklist;
    
    @jakarta.annotation.PostConstruct
    public void init() {
        this.secretKey = getSigningKey();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 检查Token是否在黑名单中
            if (tokenBlacklist.isBlacklisted(token)) {
                return false;
            }
            
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 使Token失效（加入黑名单）
     */
    public void invalidateToken(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            tokenBlacklist.addToBlacklist(token, expiration.getTime());
        } catch (Exception e) {
            // 如果Token无效，直接忽略
        }
    }

    /**
     * 获取 Claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
