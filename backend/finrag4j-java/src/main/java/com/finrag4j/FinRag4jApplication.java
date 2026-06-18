package com.finrag4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * FinRag4j Java主服务启动类
 * 
 * 功能说明：
 * - 企业级大模型RAG应用框架核心服务
 * - 承载全部业务逻辑、RAG检索、Agent流程、权限管理、审计日志
 * - 对接Python预处理服务（文档解析、OCR、分块）
 * - 支持国产信创JDK和ARM架构
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class FinRag4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinRag4jApplication.class, args);
        System.out.println("========================================");
        System.out.println("FinRag4j Java主服务启动成功！");
        System.out.println("接口文档地址: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}