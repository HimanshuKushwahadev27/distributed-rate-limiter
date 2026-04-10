package com.emi.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.emi.infracore.ratelimiter.RateLimiterStore;
import com.emi.infracore.util.KeyBuilder;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RedisRateLimiterFilter implements GlobalFilter, Ordered {
  
  private final RateLimiterStore rateLimiterStore;

  private static final int LIMIT =5 ;
  private static final int WINDOW = 60; // 1 minute
  private final KeyBuilder keyBuilder;

  @Override
  public int getOrder() {
    return -1; // Ensure this filter runs before authentication
  }
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

    String userId = getUserId(exchange);
    String  api = normalizePath(exchange);
    String window =getCurrentWindow();

    String key = keyBuilder.rateLimitKey(userId, api, window);

    long count = rateLimiterStore.incrementRequestCount(key, WINDOW);

    if(count > LIMIT) {
      exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
      return exchange.getResponse().setComplete();
    }

    return chain.filter(exchange);
  }


  private String getUserId(ServerWebExchange exchange) {
    String userId = exchange.getRequest().getHeaders().getFirst("User-Id");
    return userId != null ? userId : "anonymous";
  }

  private String normalizePath(ServerWebExchange exchange) {

    return exchange
                  .getRequest()
                  .getPath()
                  .value()
                  .replaceAll("/", "_");
  }

  private String getCurrentWindow() {
    return String.valueOf(System.currentTimeMillis()/60000);
  }




}
