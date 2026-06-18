package com.finrag4j.client.python;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Python服务配置类
 * 
 * 功能说明：
 * - 配置Python预处理服务连接信息
 * - 支持超时和重试配置
 * - 支持负载均衡（多实例）
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "python.service")
public class PythonServiceConfig {

    /**
     * Python服务地址（支持多个，逗号分隔）
     */
    private String baseUrl = "http://localhost:8001";

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout = 30000;

    /**
     * 重试次数
     */
    private Integer retryTimes = 3;

    /**
     * 重试间隔（毫秒）
     */
    private Integer retryInterval = 1000;
}