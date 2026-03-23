package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RPA 爬虫管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.example.rpa.mapper")
public class RpaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpaDemoApplication.class, args);
        System.out.println("====================================");
        System.out.println("RPA 爬虫管理系统启动成功！");
        System.out.println("接口地址：http://localhost:8080/api");
        System.out.println("====================================");
    }

}
