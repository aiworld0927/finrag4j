package com.finrag4j.search.controller;

import com.finrag4j.search.service.RagRetrievalService;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RAG检索控制器
 */
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
@Tag(name = "RAG检索", description = "向量检索、混合检索、结果重排")
public class RagRetrievalController {

    private final RagRetrievalService retrievalService;

    @PostMapping("/retrieve")
    @Operation(summary = "检索", description = "基于向量相似度和关键词混合检索")
    public Result<List<Map<String, Object>>> retrieve(@RequestBody RetrieveRequest request) {
        return Result.success(retrievalService.retrieve(request));
    }

    @PostMapping("/search")
    @Operation(summary = "语义搜索", description = "纯语义向量搜索")
    public Result<List<Map<String, Object>>> semanticSearch(@RequestBody SemanticSearchRequest request) {
        return Result.success(retrievalService.semanticSearch(request.getQuery(), request.getKbId(), request.getTopK()));
    }

    @PostMapping("/keyword-search")
    @Operation(summary = "关键词搜索", description = "基于BM25的关键词搜索")
    public Result<List<Map<String, Object>>> keywordSearch(@RequestBody KeywordSearchRequest request) {
        return Result.success(retrievalService.keywordSearch(request.getQuery(), request.getKbId(), request.getTopK()));
    }

    @lombok.Data
    public static class RetrieveRequest {
        private String query;
        private Long kbId;
        private Integer topK = 10;
        private Float similarityThreshold = 0.7f;
        private Boolean enableRerank = true;
    }

    @lombok.Data
    public static class SemanticSearchRequest {
        private String query;
        private Long kbId;
        private Integer topK = 10;
    }

    @lombok.Data
    public static class KeywordSearchRequest {
        private String query;
        private Long kbId;
        private Integer topK = 10;
    }
}
