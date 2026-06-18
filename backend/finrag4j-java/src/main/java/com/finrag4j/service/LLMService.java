package com.finrag4j.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 大模型服务
 * 
 * 功能说明：
 * - 封装大模型调用接口
 * - 支持多轮对话
 * - 支持系统提示词
 * - 支持模型路由
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@Service
@Tag(name = "大模型服务")
public class LLMService {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    /**
     * 单轮对话
     * 
     * @param userMessage 用户消息
     * @return AI回复
     */
    @Operation(summary = "单轮对话")
    public String chat(String userMessage) {
        log.info("大模型调用，用户消息: {}", userMessage);
        
        try {
            Response<AiMessage> response = chatLanguageModel.generate(userMessage);
            String aiMessage = response.content().text();
            
            log.info("大模型回复: {}", aiMessage);
            return aiMessage;
            
        } catch (Exception e) {
            log.error("大模型调用失败", e);
            throw new RuntimeException("大模型调用失败: " + e.getMessage());
        }
    }

    /**
     * 多轮对话
     * 
     * @param messages 消息列表
     * @return AI回复
     */
    @Operation(summary = "多轮对话")
    public String chat(List<ChatMessage> messages) {
        log.info("大模型调用，消息数量: {}", messages.size());
        
        try {
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            String aiMessage = response.content().text();
            
            log.info("大模型回复: {}", aiMessage);
            return aiMessage;
            
        } catch (Exception e) {
            log.error("大模型调用失败", e);
            throw new RuntimeException("大模型调用失败: " + e.getMessage());
        }
    }

    /**
     * 带系统提示词的对话
     * 
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @return AI回复
     */
    @Operation(summary = "带系统提示词的对话")
    public String chatWithSystem(String systemPrompt, String userMessage) {
        log.info("大模型调用，系统提示词: {}, 用户消息: {}", systemPrompt, userMessage);
        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(systemPrompt));
        messages.add(UserMessage.from(userMessage));
        
        return chat(messages);
    }

    /**
     * RAG对话（带上下文）
     * 
     * @param question 用户问题
     * @param context 相关文档上下文
     * @return AI回复
     */
    @Operation(summary = "RAG对话")
    public String ragChat(String question, String context) {
        String systemPrompt = "你是一个专业的金融助手。请基于以下参考文档回答用户问题，不要编造信息。如果参考文档中没有相关信息，请如实告知。\n\n参考文档：\n" + context;
        
        return chatWithSystem(systemPrompt, question);
    }

    /**
     * 文本向量化
     * 
     * @param text 待向量化的文本
     * @return 向量数组
     */
    @Operation(summary = "文本向量化")
    public float[] embed(String text) {
        log.info("文本向量化，文本长度: {}", text.length());
        
        // TODO: 集成embedding模型
        // 目前使用简单的hash方式生成向量，实际应使用专业的embedding模型
        return generateSimpleEmbedding(text);
    }

    /**
     * 生成简单向量（临时方案）
     * 实际应使用专业的embedding模型
     */
    private float[] generateSimpleEmbedding(String text) {
        // 生成768维向量（常见embedding模型维度）
        float[] vector = new float[768];
        int hash = text.hashCode();
        
        for (int i = 0; i < 768; i++) {
            vector[i] = (hash % 1000) / 1000.0f;
            hash = hash * 31 + i;
        }
        
        // 归一化
        float norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        
        for (int i = 0; i < 768; i++) {
            vector[i] /= norm;
        }
        
        return vector;
    }
}