package com.kaotu.user.filter;


import com.kaotu.base.context.UserContext;
import com.kaotu.base.utils.JwtUtil;
import com.kaotu.base.utils.LogUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {


    // 从 application.yml 文件中注入JWT密钥
    @Value("${kaotu.jwt.secret-key}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("JWT认证过滤器开始处理请求: {}", request.getRequestURI());

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
                String userId = claims.get("userId").toString();
                if (userId != null) {
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
                LogUtils.error("JWT解析失败: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                // 返回具体的错误信息
                response.getWriter().write("{\"code\": 401, \"message\": \"无效的Token或Token已过期\"}");
                return;
            }
        }

        // 7. 无论 token 是否有效，都继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}