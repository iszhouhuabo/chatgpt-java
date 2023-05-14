package com.github.iszhouhuabo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author louye
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.github.iszhouhuabo.mapper")
@RestController
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    @GetMapping("/")
    public String index() {
        return "请勿直接访问后台,请使用前端访问!";
    }
}
