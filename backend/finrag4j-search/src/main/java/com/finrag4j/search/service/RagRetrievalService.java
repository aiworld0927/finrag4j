package com.finrag4j.search.service;

import com.finrag4j.search.controller.RagRetrievalController.RetrieveRequest;
import com.finrag4j.search.entity.VectorChunk;
import com.finrag4j.search.mapper.VectorChunkMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG检索服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalService {

    private final VectorChunkMapper vectorChunkMapper;
    private final EmbeddingService embeddingService;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 混合检索
     */
    public List<Map<String, Object>> retrieve(RetrieveRequest request) {
        // 1. 语义向量检索
        List<Map<String, Object>> semanticResults = semanticSearch(
                request.getQuery(), request.getKbId(), request.getTopK()
        );

        // 2. 关键词检索
        List<Map<String, Object>> keywordResults = keywordSearch(
                request.getQuery(), request.getKbId(), request.getTopK()
        );

        // 3. 结果融合
        List<Map<String, Object>> fusedResults = fuseResults(
                semanticResults, keywordResults, request.getEnableRerank()
        );

        // 4. 过滤相似度阈值
        if (request.getSimilarityThreshold() != null) {
            fusedResults = fusedResults.stream()
                    .filter(r -> (Float) r.getOrDefault("score", 0f) >= request.getSimilarityThreshold())
                    .collect(Collectors.toList());
        }

        return fusedResults;
    }

    /**
     * 语义向量搜索
     */
    public List<Map<String, Object>> semanticSearch(String query, Long kbId, Integer topK) {
        // 生成查询向量
        float[] queryEmbedding = embeddingService.embed(query);

        // 查询向量数据库
        // TODO: 使用PGVector的向量相似度搜索 SQL
        LambdaQueryWrapper<VectorChunk> wrapper = new LambdaQueryWrapper<>();
        if (kbId != null) {
            wrapper.eq(VectorChunk::getKbId, kbId);
        }
        wrapper.eq(VectorChunk::getDeleted, 0);

        List<VectorChunk> chunks = vectorChunkMapper.selectList(wrapper);

        // 计算相似度并排序
        List<Map<String, Object>> results = new ArrayList<>();
        for (VectorChunk chunk : chunks) {
            float[] chunkVector = parseVector(chunk.getVector());
            if (chunkVector != null) {
                float similarity = embeddingService.cosineSimilarity(queryEmbedding, chunkVector);
                Map<String, Object> item = new HashMap<>();
                item.put("chunkId", chunk.getId());
                item.put("documentId", chunk.getDocumentId());
                item.put("content", chunk.getContent());
                item.put("score", similarity);
                item.put("metadata", chunk.getMetadata());
                results.add(item);
            }
        }

        // 按相似度排序
        results.sort((a, b) -> Float.compare((Float) b.get("score"), (Float) a.get("score")));

        // 返回TopK
        return results.stream().limit(topK).collect(Collectors.toList());
    }

    /**
     * 关键词搜索（BM25）
     */
    public List<Map<String, Object>> keywordSearch(String query, Long kbId, Integer topK) {
        // TODO: 实现BM25算法
        // 目前简单实现：按关键词匹配
        LambdaQueryWrapper<VectorChunk> wrapper = new LambdaQueryWrapper<>();
        if (kbId != null) {
            wrapper.eq(VectorChunk::getKbId, kbId);
        }
        wrapper.eq(VectorChunk::getDeleted, 0);

        // 简单的关键词匹配
        String[] keywords = query.toLowerCase().split("\\s+");
        List<VectorChunk> chunks = vectorChunkMapper.selectList(wrapper);

        List<Map<String, Object>> results = new ArrayList<>();
        for (VectorChunk chunk : chunks) {
            String content = chunk.getContent().toLowerCase();
            int matchCount = 0;
            for (String keyword : keywords) {
                if (content.contains(keyword)) {
                    matchCount++;
                }
            }
            if (matchCount > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("chunkId", chunk.getId());
                item.put("documentId", chunk.getDocumentId());
                item.put("content", chunk.getContent());
                item.put("score", (float) matchCount / keywords.length);
                item.put("metadata", chunk.getMetadata());
                results.add(item);
            }
        }

        results.sort((a, b) -> Float.compare((Float) b.get("score"), (Float) a.get("score")));
        return results.stream().limit(topK).collect(Collectors.toList());
    }

    /**
     * 结果融合（RRF算法）
     */
    private List<Map<String, Object>> fuseResults(List<Map<String, Object>> semanticResults,
                                                   List<Map<String, Object>> keywordResults,
                                                   boolean enableRerank) {
        Map<Long, Map<String, Object>> scoreMap = new HashMap<>();
        Map<Long, Float> rankScores = new HashMap<>();

        // 计算语义搜索的RRF分数
        for (int i = 0; i < semanticResults.size(); i++) {
            Map<String, Object> r = semanticResults.get(i);
            Long chunkId = (Long) r.get("chunkId");
            float rrfScore = 1.0f / (60 + i + 1);  // RRF k=60
            rankScores.merge(chunkId, rrfScore, Float::sum);
            scoreMap.put(chunkId, r);
        }

        // 计算关键词搜索的RRF分数
        for (int i = 0; i < keywordResults.size(); i++) {
            Map<String, Object> r = keywordResults.get(i);
            Long chunkId = (Long) r.get("chunkId");
            float rrfScore = 1.0f / (60 + i + 1);
            rankScores.merge(chunkId, rrfScore, Float::sum);
            scoreMap.putIfAbsent(chunkId, r);
        }

        // 按融合分数排序
        List<Map<String, Object>> fused = new ArrayList<>();
        rankScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Float>comparingByValue().reversed())
                .forEach(e -> {
                    Map<String, Object> item = scoreMap.get(e.getKey());
                    item.put("score", e.getValue());
                    fused.add(item);
                });

        return fused;
    }

    private float[] parseVector(String vectorStr) {
        if (vectorStr == null || vectorStr.isEmpty()) {
            return null;
        }
        try {
            // 解析 "[v1,v2,...]" 格式
            String[] parts = vectorStr.substring(1, vectorStr.length() - 1).split(",");
            float[] vector = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                vector[i] = Float.parseFloat(parts[i].trim());
            }
            return vector;
        } catch (Exception e) {
            return null;
        }
    }
}
