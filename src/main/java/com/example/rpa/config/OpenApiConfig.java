package com.example.rpa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rpaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RPA 爬虫运营管理系统 API")
                        .description("RPA 爬虫运营管理系统后端接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("RPA Team"))
                        .license(new License()
                                .name("Apache 2.0")))
                .servers(List.of(new Server()
                        .url("/api")
                        .description("Default Server")));
    }

    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/**")
                .build();
    }
}
