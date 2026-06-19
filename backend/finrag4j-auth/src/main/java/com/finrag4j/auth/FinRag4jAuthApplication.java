package com.finrag4j.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * FinRag4j 认证授权服务
 * 职责: 用户管理、角色权限、RBAC、JWT认证
 */
@SpringBootApplication(scanBasePackages = "com.finrag4j")
@MapperScan("com.finrag4j.auth.mapper")
@EnableDiscoveryClient
public class FinRag4jAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinRag4jAuthApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
