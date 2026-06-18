package com.finrag4j.service;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.*;
import com.finrag4j.mapper.DocumentMapper;
import com.finrag4j.mapper.KbDocumentMapper;
import com.finrag4j.mapper.VectorChunkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG检索核心服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalService {

    private final VectorService vectorService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final DocumentMapper documentMapper;
    private final KbDocumentMapper kbDocumentMapper;

    /**
     * RAG检索结果
     */
    public record RagResult(
            List<RetrievedChunk> chunks,
            Double avgSimilarity,
            String context,
            List<SourceReference> references
    ) {}

    /**
     * 检索到的文本块
     */
    public record RetrievedChunk(
            Long documentId,
            String documentName,
            String content,
            Integer chunkIndex,
            Double similarity
    ) {}

    /**
     * 来源引用
     */
    public record SourceReference(
            String fileName,
            Integer pageNumber,
            Double similarity,
            String snippet
    ) {}

    /**
     * 执行RAG检索
     */
    public RagResult retrieve(String question, Long kbId, Long tenantId) {
        // 1. 获取知识库配置
        KnowledgeBase kb = knowledgeBaseService.getById(kbId);
        if (kb == null) {
            throw new BusinessException("知识库不存在");
        }

        Double threshold = kb.getSimilarityThreshold() != null ? kb.getSimilarityThreshold() : 0.7;
        Integer topK = kb.getTopK() != null ? kb.getTopK() : 5;

        // 2. 获取知识库绑定的文档ID
        List<KbDocument> kbDocuments = kbDocumentMapper.selectByKbId(kbId, tenantId);
        if (kbDocuments.isEmpty()) {
            log.warn("知识库 {} 没有绑定任何文档", kb.getKbName());
            return new RagResult(Collections.emptyList(), 0.0, "", Collections.emptyList());
        }

        List<Long> documentIds = kbDocuments.stream()
                .map(KbDocument::getDocumentId)
                .collect(Collectors.toList());

        // 3. 向量检索
        List<VectorChunk> results = vectorService.searchByDocumentIds(
                question, documentIds, tenantId, topK, threshold
        );

        if (results.isEmpty()) {
            log.warn("没有检索到相关内容");
            return new RagResult(Collections.emptyList(), 0.0, "", Collections.emptyList());
        }

        // 4. 获取文档信息用于溯源
        Map<Long, Document> documentMap = new HashMap<>();
        for (Long docId : documentIds) {
            Document doc = documentMapper.selectById(docId);
            if (doc != null) {
                documentMap.put(docId, doc);
            }
        }

        // 5. 构建检索结果
        List<RetrievedChunk> chunks = new ArrayList<>();
        List<SourceReference> references = new ArrayList<>();
        double totalSimilarity = 0;

        for (VectorChunk chunk : results) {
            Document doc = documentMap.get(chunk.getDocumentId());
            String docName = doc != null ? doc.getFileName() : "未知文档";

            RetrievedChunk retrievedChunk = new RetrievedChunk(
                    chunk.getDocumentId(),
                    docName,
                    chunk.getContent(),
                    chunk.getChunkIndex(),
                    chunk.getSimilarity() != null ? chunk.getSimilarity() : 0.0
            );
            chunks.add(retrievedChunk);

            SourceReference reference = new SourceReference(
                    docName,
                    null, // 页码信息需要从解析结果中获取
                    chunk.getSimilarity() != null ? chunk.getSimilarity() : 0.0,
                    truncateContent(chunk.getContent(), 100)
            );
            references.add(reference);

            totalSimilarity += chunk.getSimilarity() != null ? chunk.getSimilarity() : 0.0;
        }

        // 6. 结果重排（按相似度排序）
        chunks.sort((a, b) -> Double.compare(b.similarity(), a.similarity()));

        // 7. 拼接上下文
        String context = chunks.stream()
                .map(RetrievedChunk::content)
                .collect(Collectors.joining("\n\n---\n\n"));

        double avgSimilarity = chunks.isEmpty() ? 0.0 : totalSimilarity / chunks.size();

        log.info("RAG检索完成: 找到 {} 个相关片段, 平均相似度: {}", chunks.size(), avgSimilarity);

        return new RagResult(chunks, avgSimilarity, context, references);
    }

    /**
     * 截断内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
    }

    /**
     * 批量向量检索（多个知识库）
     */
    public RagResult retrieveMultiKb(String question, List<Long> kbIds, Long tenantId) {
        List<RetrievedChunk> allChunks = new ArrayList<>();
        List<SourceReference> allReferences = new ArrayList<>();
        double totalSimilarity = 0;

        for (Long kbId : kbIds) {
            try {
                RagResult result = retrieve(question, kbId, tenantId);
                allChunks.addAll(result.chunks());
                allReferences.addAll(result.references());
                totalSimilarity += result.avgSimilarity() * result.chunks().size();
            } catch (Exception e) {
                log.warn("检索知识库 {} 失败: {}", kbId, e.getMessage());
            }
        }

        // 合并去重并排序
        Map<String, RetrievedChunk> uniqueChunks = new LinkedHashMap<>();
        for (RetrievedChunk chunk : allChunks) {
            String key = chunk.documentId() + "_" + chunk.chunkIndex();
            uniqueChunks.put(key, chunk);
        }

        List<RetrievedChunk> sortedChunks = new ArrayList<>(uniqueChunks.values());
        sortedChunks.sort((a, b) -> Double.compare(b.similarity(), a.similarity()));

        // 取前N个
        int maxResults = 5;
        if (sortedChunks.size() > maxResults) {
            sortedChunks = sortedChunks.subList(0, maxResults);
        }

        String context = sortedChunks.stream()
                .map(RetrievedChunk::content)
                .collect(Collectors.joining("\n\n---\n\n"));

        double avgSimilarity = sortedChunks.isEmpty() ? 0.0 : totalSimilarity / sortedChunks.size();

        return new RagResult(sortedChunks, avgSimilarity, context, allReferences);
    }
}