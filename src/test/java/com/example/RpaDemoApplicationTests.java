package com.example;

import com.example.rpa.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RpaDemoApplicationTests {
    @Autowired
    private PasswordUtil passwordUtil;
    @Test
    void contextLoads() {
        String string = passwordUtil.encodePassword("admin123");
        System.out.println(string);
    }

}
