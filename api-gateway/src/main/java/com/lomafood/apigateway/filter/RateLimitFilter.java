package com.lomafood.apigateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    // Max requests per minute per endpoint
    private static final Map<String, Integer> ENDPOINT_LIMITS = Map.of(
        "/auth/login", 5,
        "/auth/register", 3,
        "/auth/forgot-password", 3,
        "/auth/verify-otp", 5,
        "/auth/resend-otp", 2
    );

    private static final int DEFAULT_LIMIT = 30;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String ip = getClientIp(exchange);
        String key = "rate:" + ip + ":" + path;

        int limit = ENDPOINT_LIMITS.getOrDefault(path, DEFAULT_LIMIT);

        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // Set expiry on first request
                        return redisTemplate.expire(key, Duration.ofMinutes(1))
                                .then(chain.filter(exchange));
                    }
                    if (count > limit) {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().add(
                            "X-RateLimit-Limit", String.valueOf(limit)
                        );
                        exchange.getResponse().getHeaders().add(
                            "X-RateLimit-Remaining", "0"
                        );
                        exchange.getResponse().getHeaders().add(
                            "Retry-After", "60"
                        );
                        return exchange.getResponse().setComplete();
                    }
                    exchange.getResponse().getHeaders().add(
                        "X-RateLimit-Limit", String.valueOf(limit)
                    );
                    exchange.getResponse().getHeaders().add(
                        "X-RateLimit-Remaining", String.valueOf(limit - count)
                    );
                    return chain.filter(exchange);
                });
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -2; // Run before AuthFilter
    }
}
