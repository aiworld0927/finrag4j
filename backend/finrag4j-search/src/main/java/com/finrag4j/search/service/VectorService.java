package com.finrag4j.search.service;

import com.finrag4j.search.entity.VectorChunk;
import com.finrag4j.search.mapper.VectorChunkMapper;
import com.finrag4j.common.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 向量服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService extends ServiceImpl<VectorChunkMapper, VectorChunk> {

    private final VectorChunkMapper vectorChunkMapper;
    private final EmbeddingService embeddingService;

    /**
     * 添加向量片段
     */
    @Transactional
    public void addChunk(VectorChunk chunk) {
        // 生成向量
        float[] embedding = embeddingService.embed(chunk.getContent());
        chunk.setVector(toVectorString(embedding));

        vectorChunkMapper.insert(chunk);
        log.info("Added chunk {} for document {}", chunk.getId(), chunk.getDocumentId());
    }

    /**
     * 批量添加向量片段
     */
    @Transactional
    public void addChunks(List<VectorChunk> chunks) {
        for (VectorChunk chunk : chunks) {
            addChunk(chunk);
        }
    }

    /**
     * 删除向量片段
     */
    public void deleteChunk(Long id) {
        vectorChunkMapper.deleteById(id);
    }

    /**
     * 根据文档ID删除所有向量
     */
    public void deleteByDocumentId(Long docId) {
        vectorChunkMapper.delete(
                new LambdaQueryWrapper<VectorChunk>()
                        .eq(VectorChunk::getDocumentId, docId)
        );
    }

    /**
     * 重建向量索引
     */
    @Transactional
    public void rebuildIndex(Long kbId) {
        // 获取该知识库的所有chunk
        List<VectorChunk> chunks = vectorChunkMapper.selectList(
                new LambdaQueryWrapper<VectorChunk>()
                        .eq(VectorChunk::getKbId, kbId)
        );

        // 重新生成向量
        for (VectorChunk chunk : chunks) {
            float[] embedding = embeddingService.embed(chunk.getContent());
            chunk.setVector(toVectorString(embedding));
            vectorChunkMapper.updateById(chunk);
        }

        log.info("Rebuilt index for kb {}, total {} chunks", kbId, chunks.size());
    }

    /**
     * 搜索相似的向量
     */
    public List<VectorChunk> searchSimilar(float[] queryEmbedding, Long kbId, int topK, float threshold) {
        // TODO: 使用PGVector的向量相似度搜索
        return null;
    }

    private String toVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
