package com.kaotu.admin.config;

import com.kaotu.admin.interceptor.AdminInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/**") // 拦截所有请求路径
                .excludePathPatterns("/admin/login") // 排除登录、注册接口
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**"); // 排除Swagger相关路径
        log.info("AdminInterceptor registered successfully.");
    }

}
