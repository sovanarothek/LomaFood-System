package com.lomafood.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        String ip = getClientIp(exchange);
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String userEmail = request.getHeaders().getFirst("X-User-Email");

        log.info("→ REQUEST | IP: {} | Method: {} | Path: {} | User: {}",
                ip, method, path, userEmail != null ? userEmail : "anonymous");

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatusCode() != null
                    ? response.getStatusCode().value() : 0;

            log.info("← RESPONSE | IP: {} | Method: {} | Path: {} | Status: {} | Duration: {}ms",
                    ip, method, path, status, duration);
        }));
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
        return -3; // Run before everything
    }
}
