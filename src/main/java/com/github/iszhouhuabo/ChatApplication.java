package com.github.iszhouhuabo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author louye
 */
@SpringBootApplication
@EnableScheduling
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    @GetMapping("")
    public String index() {
        return "1.html";
    }

    @GetMapping("/websocket")
    public String websocket() {
        return "websocket.html";
    }
}
