package com.kaotu.admin.interceptor;

import com.kaotu.base.properties.JwtProperties;
import com.kaotu.base.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头中获取令牌
        // 这里的 "token" 是前后端约定好的请求头名称
        String token = request.getHeader("Authorization");
        log.info("token:{}", token);
        if (token == null || !token.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return  false;
        }
        // 2. 校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token.substring(7));
            if(claims.get("adminId")==null){
                log.error("令牌校验不通过");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return  false;
            }
            return true;
        } catch (Exception ex) {
            // 4. 不通过，响应401状态码
            log.error("令牌校验不通过: ", ex);
            response.setStatus(401);
            return false;
        }
    }


}
