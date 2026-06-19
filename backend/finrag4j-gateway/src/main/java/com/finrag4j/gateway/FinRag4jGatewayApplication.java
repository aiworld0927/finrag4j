package com.finrag4j.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * FinRag4j API网关服务
 * 职责: 请求路由、负载均衡、认证鉴权、限流熔断
 */
@SpringBootApplication
@EnableDiscoveryClient
public class FinRag4jGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinRag4jGatewayApplication.class, args);
    }
}
