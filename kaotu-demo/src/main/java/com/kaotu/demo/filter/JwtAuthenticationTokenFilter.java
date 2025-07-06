package com.kaotu.demo.filter;


import com.kaotu.demo.context.UserContext;
import com.kaotu.demo.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * <p>
 * 继承 OncePerRequestFilter 确保每次请求只执行一次过滤。
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    // 从 application.properties 或 application.yml 文件中注入JWT密钥
//    @Value("${jwt.secretKey}")
    private final String secretKey="itkaotu";
//    private Long ttl=7200000L;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从请求头获取 "Authorization"
        String authHeader = request.getHeader("Authorization");

        // 2. 检查 token 是否存在且格式正确 (以 "Bearer " 开头)
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 截取 "Bearer " 后的部分

            try {
                // 3. 使用 JwtUtil 解析 token
                Claims claims = JwtUtil.parseJWT(secretKey, token);

                // 4. 从 claims 中获取用户信息，这里假设用户ID存储在名为 "userId" 的 claim 中
                // 注意：这里的 key ("userId") 必须与生成 token 时放入的 key 一致
                Object userIdObj = claims.get("userId");
                if (userIdObj != null) {
                    String userId = String.valueOf(userIdObj);
                    UserContext.setUserId(userId);
                    // 5. 创建 Authentication 对象
                    // 参数：principal (用户标识), credentials (通常为null), authorities (权限列表)
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

                    // 6. 将 Authentication 对象存入 SecurityContextHolder
                    // 后续的程序就可以通过 SecurityContextHolder.getContext().getAuthentication() 获取到已认证的用户信息
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception e) {
                // Token 解析失败 (例如：过期、签名错误等)，不做任何处理
                // 请求将以匿名用户身份继续，这对于公开接口是必要的
                logger.warn("JWT token parse error: {}", e.getMessage());
            }
        }

        // 7. 无论 token 是否有效，都继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}