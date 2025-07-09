package com.kaotu.user.config;


import com.kaotu.user.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;


    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 禁用 CSRF 保护
                .authorizeRequests()
                .antMatchers("/user/user/login", "/user/user/register","/user/book/**","/user/user/personalize").permitAll()
                .antMatchers("/user/post/tags","/user/post/detail","/user/post/recommend","/user/post/comments",
                        "/user/post/search","/user/post/hotTags","/user/post/hotPosts","/user/post/new",
                        "/webjars/**","/swagger-ui/**","/v3/api-docs/**").permitAll()
                .anyRequest().authenticated() // 其他请求需要认证
                .and()
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class); // 添加 JWT 过滤器
        return http.build();
    }

}