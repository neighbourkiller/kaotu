package com.kaotu.user.config;


import com.kaotu.user.interceptor.UserIdInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类 ！
 * <p>
 * 主要用于配置消息转换器，使用FastJson作为JSON处理工具
 * </p>
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserIdInterceptor userIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userIdInterceptor)
                .addPathPatterns("/**") // 拦截所有请求路径
                .excludePathPatterns("swagger-ui.html", "/v3/api-docs/**", "/webjars/**", "/error") // 排除Swagger相关路径和错误处理路径
                .excludePathPatterns("/user/book","/user/book/**") // 排除书籍相关接口
                .excludePathPatterns("/user/user/login", "/user/user/register"); // 排除登录、注册和忘记密码接口
    }
}
