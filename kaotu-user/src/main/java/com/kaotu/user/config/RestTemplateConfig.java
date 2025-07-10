package com.kaotu.user.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
/*                // 设置连接超时时间为5秒
                .setConnectTimeout(Duration.ofSeconds(5))
                // 设置读取超时时间为5秒
                .setReadTimeout(Duration.ofSeconds(5))*/
                .build();
    }
}