package com.finrag4j.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 网关路由配置
 */
@Configuration
public class GatewayConfig {

    /**
     * 基于用户ID的限流Key解析器
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId == null) {
                return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
            }
            return Mono.just(userId);
        };
    }

    /**
     * Redis限流器配置
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // 默认每秒100个请求
        return new RedisRateLimiter(100, 200);
    }
}
