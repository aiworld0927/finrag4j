package com.finrag4j.document;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * FinRag4j 文档服务
 * 职责: 文档上传、存储、解析、管理、版本控制
 */
@SpringBootApplication(scanBasePackages = "com.finrag4j")
@MapperScan("com.finrag4j.document.mapper")
@EnableFeignClients(basePackages = "com.finrag4j.document.client")
@EnableDiscoveryClient
public class FinRag4jDocumentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinRag4jDocumentApplication.class, args);
    }
}
