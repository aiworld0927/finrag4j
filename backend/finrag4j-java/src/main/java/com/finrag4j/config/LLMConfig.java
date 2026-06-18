package com.finrag4j.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 大模型配置类
 * 
 * 功能说明：
 * - 配置Ollama/vLLM连接
 * - 支持多模型管理
 * - 支持模型路由
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LLMConfig {

    /**
     * 模型类型：ollama/vllm/openai
     */
    private String type = "ollama";

    /**
     * 模型服务地址
     */
    private String baseUrl = "http://localhost:11434";

    /**
     * 默认模型名称
     */
    private String defaultModel = "qwen2:7b";

    /**
     * 超时时间（秒）
     */
    private Integer timeout = 60;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 温度参数（0-2）
     */
    private Double temperature = 0.7;

    /**
     * 最大token数
     */
    private Integer maxTokens = 2000;

    /**
     * 创建Ollama聊天模型
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if ("ollama".equalsIgnoreCase(type)) {
            return OllamaChatModel.builder()
                    .baseUrl(baseUrl)
                    .modelName(defaultModel)
                    .timeout(Duration.ofSeconds(timeout))
                    .temperature(temperature)
                    .maxRetries(maxRetries)
                    .build();
        }
        
        // 支持其他模型类型扩展
        throw new UnsupportedOperationException("不支持的模型类型: " + type);
    }
}