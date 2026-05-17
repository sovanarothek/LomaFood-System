package com.lomafood.apigateway.filter;

import com.lomafood.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // Public endpoints that don't need JWT
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/auth/register",
        "/auth/login",
        "/auth/verify-otp",
        "/auth/resend-otp",
        "/auth/forgot-password",
        "/auth/reset-password",
        "/auth/google",
        "/auth/refresh-token",
        "/swagger-ui",
        "/v3/api-docs",
        "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Allow public endpoints
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        // Check Authorization header
        HttpHeaders headers = exchange.getRequest().getHeaders();
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Add user info to headers for downstream services
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                        .build())
                .build();

        return chain.filter(mutatedExchange);
    }

    private boolean isPublic(String path) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
