package com.game;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.game.mapper")
public class SpringbootVertxGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootVertxGameApplication.class, args);
    }

}