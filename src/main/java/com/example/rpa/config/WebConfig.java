package com.example.rpa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置类 - 跨域配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarUploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path avatarPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
        String avatarLocation = avatarPath.toUri().toString();
        if (!avatarLocation.endsWith("/")) {
            avatarLocation += "/";
        }
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatarLocation);
    }
}
