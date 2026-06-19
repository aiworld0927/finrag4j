package com.finrag4j.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * LLM大模型服务
 */
@Slf4j
@Service
public class LlmService {

    @Value("${langchain4j.model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.model.model-name}")
    private String modelName;

    /**
     * 调用大模型
     */
    public String chat(String prompt) {
        // TODO: 使用LangChain4j调用Ollama/vLLM等大模型
        log.info("Calling LLM with prompt length: {}", prompt.length());

        // 模拟返回
        return "这是AI的回复。目前集成LangChain4j的完整实现待完成。";
    }

    /**
     * 流式调用大模型
     */
    public void streamChat(String prompt, StreamCallback callback) {
        // TODO: 实现流式调用
        String response = chat(prompt);
        callback.onChunk(response);
        callback.onComplete();
    }

    public interface StreamCallback {
        void onChunk(String chunk);
        void onComplete();
    }
}
