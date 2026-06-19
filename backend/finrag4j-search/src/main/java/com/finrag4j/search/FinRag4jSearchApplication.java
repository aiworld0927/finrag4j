package com.finrag4j.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * FinRag4j 检索服务
 * 职责: 文本向量化、向量存储、相似度检索、混合检索
 */
@SpringBootApplication(scanBasePackages = "com.finrag4j")
@MapperScan("com.finrag4j.search.mapper")
@EnableDiscoveryClient
public class FinRag4jSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinRag4jSearchApplication.class, args);
    }
}
