package com.emi.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class IdempotencyFilter implements GlobalFilter, Ordered{
    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    @Override
    public int getOrder() {
        return -2; 
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String idempotencyKey =
                exchange.getRequest().getHeaders().getFirst(IDEMPOTENCY_HEADER);

        if (exchange.getRequest().getMethod() == HttpMethod.POST) {

            if (idempotencyKey == null || idempotencyKey.isBlank()) {

                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                DataBuffer buffer = exchange.getResponse()
                        .bufferFactory()
                        .wrap("Missing Idempotency-Key header".getBytes());

                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
        }

        return chain.filter(exchange);
    }
}
