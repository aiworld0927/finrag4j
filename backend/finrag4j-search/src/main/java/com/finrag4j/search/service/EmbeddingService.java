package com.finrag4j.search.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 向量化服务
 * 使用LangChain4j调用Embedding模型
 */
@Slf4j
@Service
public class EmbeddingService {

    @Value("${langchain4j.model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.model.model-name}")
    private String modelName;

    /**
     * 生成文本向量
     */
    public float[] embed(String text) {
        // TODO: 使用LangChain4j的Embedding模型生成向量
        // 目前是占位实现，实际需要集成Ollama或其他Embedding服务
        log.info("Generating embedding for text of length: {}", text.length());

        // 返回一个假的768维向量作为占位
        int dimensions = 768;
        float[] embedding = new float[dimensions];
        for (int i = 0; i < dimensions; i++) {
            embedding[i] = (float) (Math.random() * 2 - 1);
        }

        // 归一化
        float norm = 0;
        for (float v : embedding) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        for (int i = 0; i < dimensions; i++) {
            embedding[i] /= norm;
        }

        return embedding;
    }

    /**
     * 计算余弦相似度
     */
    public float cosineSimilarity(float[] a, float[] b) {
        float dotProduct = 0;
        float normA = 0;
        float normB = 0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dotProduct / ((float) Math.sqrt(normA) * (float) Math.sqrt(normB));
    }
}
