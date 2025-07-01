package com.kaotu.filter;

import com.kaotu.base.properties.AuthProperties;
import com.kaotu.base.properties.JwtProperties;
import com.kaotu.base.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TokenValidationFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProps;
    private final JwtProperties jwtProps;
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Autowired
    public TokenValidationFilter(AuthProperties authProps, JwtProperties jwtProps) {
        this.authProps = authProps;
        this.jwtProps = jwtProps;
    }

    private boolean isWhitelisted(String path) {
        return authProps.getWhitelist().stream().anyMatch(p -> matcher.match(p, path));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.info("path: {}", path);
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = auth.substring(7);
        try {
            log.info("token validation: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProps.getSecretKey(), token);
            String userId = claims.get("userId").toString();
            ServerHttpRequest req = exchange.getRequest().mutate().header("userId", userId).build();
            return chain.filter(exchange.mutate().request(req).build());
        }catch (Exception ex) {
            //401 Unauthorized
            log.info("token validation fail: {}", ex.getMessage());
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}