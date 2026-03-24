package com.example.rpa.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 密码工具类
 * 提供密码加密、验证和强度校验功能
 */
@Component
public class PasswordUtil {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 密码强度要求：
     * - 至少8位
     * - 包含字母和数字
     * - 可以包含特殊字符
     */
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$";
    
    /**
     * 加密密码
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 验证密码是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 验证密码强度
     */
    public boolean validatePasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        return Pattern.matches(PASSWORD_PATTERN, password);
    }
    
    /**
     * 获取密码强度提示信息
     */
    public String getPasswordStrengthHint() {
        return "密码必须包含字母和数字，长度至少8位";
    }
    
    /**
     * 生成随机密码
     */
    public String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*#?&";
        StringBuilder password = new StringBuilder();
        
        // 确保包含字母和数字
        password.append(getRandomChar("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"));
        password.append(getRandomChar("0123456789"));
        
        // 生成剩余字符
        for (int i = 0; i < 6; i++) {
            password.append(getRandomChar(chars));
        }
        
        return password.toString();
    }
    
    private char getRandomChar(String charSet) {
        int index = (int) (Math.random() * charSet.length());
        return charSet.charAt(index);
    }
}