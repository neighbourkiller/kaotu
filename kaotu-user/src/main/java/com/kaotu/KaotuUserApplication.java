package com.kaotu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.kaotu.user.mapper")
@SpringBootApplication
public class KaotuUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaotuUserApplication.class, args);
    }

}
