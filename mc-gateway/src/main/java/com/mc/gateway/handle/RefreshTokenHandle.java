package com.mc.gateway.handle;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RefreshTokenHandle {
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, String refreshToken) {
        return chain.filter(exchange);
    }
}
