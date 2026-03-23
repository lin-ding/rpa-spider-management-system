package com.example.rpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordGenerator {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void generate() {
        String rawPassword = "admin123";
        String encoded = passwordEncoder.encode(rawPassword);
        System.out.println("明文密码: " + rawPassword);
        System.out.println("加密后的BCrypt哈希: " + encoded);
    }
}