package com.example.rpa.util;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Token黑名单管理
 * 用于管理已失效的Token，确保登出后Token无法继续使用
 */
@Component
public class TokenBlacklist {
    
    private final ConcurrentMap<String, Long> blacklist = new ConcurrentHashMap<>();
    
    /**
     * 将Token加入黑名单
     */
    public void addToBlacklist(String token, Long expirationTime) {
        blacklist.put(token, expirationTime);
    }
    
    /**
     * 检查Token是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        Long expirationTime = blacklist.get(token);
        if (expirationTime == null) {
            return false;
        }
        
        // 如果Token已过期，从黑名单中移除
        if (expirationTime < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }
        
        return true;
    }
    
    /**
     * 清理过期的黑名单Token
     */
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
    
    /**
     * 获取黑名单大小（用于监控）
     */
    public int getBlacklistSize() {
        cleanupExpiredTokens();
        return blacklist.size();
    }
}