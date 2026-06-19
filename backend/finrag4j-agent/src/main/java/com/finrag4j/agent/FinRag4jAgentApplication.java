package com.finrag4j.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * FinRag4j Agent服务
 * 职责: 智能问答、Agent编排、信贷抽取、合规分析
 */
@SpringBootApplication(scanBasePackages = "com.finrag4j")
@MapperScan("com.finrag4j.agent.mapper")
@EnableFeignClients
@EnableDiscoveryClient
public class FinRag4jAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinRag4jAgentApplication.class, args);
    }
}
