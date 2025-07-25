package com.kaotu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableCaching
@MapperScan("com.kaotu.user.mapper")
@SpringBootApplication
public class KaotuUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaotuUserApplication.class, args);
    }

}
