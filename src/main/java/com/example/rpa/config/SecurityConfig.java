package com.example.rpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** 
 * 安全配置类 - 密码编码器配置
 */
@Configuration
@EnableAspectJAutoProxy
public class SecurityConfig {
    
    /**
     * 配置 BCryptPasswordEncoder 作为密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
