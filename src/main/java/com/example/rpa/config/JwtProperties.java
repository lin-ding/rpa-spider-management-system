package com.example.rpa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥
     */
    private String secret;

    /**
     * JWT 过期时间（毫秒）默认 24 小时
     */
    private Long expiration = 86400000L;

    /**
     * JWT 请求头名称
     */
    private String header = "Authorization";

    /**
     * JWT 前缀
     */
    private String prefix = "Bearer ";
}
