package com.finrag4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finrag4j.entity.VectorChunk;
import com.finrag4j.mapper.VectorChunkMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 向量库服务
 * 
 * 功能说明：
 * - 向量增删改查
 * - 向量批量插入
 * - 向量相似度检索
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class VectorService extends ServiceImpl<VectorChunkMapper, VectorChunk> {

    /**
     * 批量插入向量
     * 
     * @param chunks 向量文本块列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<VectorChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        
        saveBatch(chunks, 100); // 每批100条
        log.info("批量插入向量成功，数量: {}", chunks.size());
    }

    /**
     * 向量相似度检索
     * 
     * @param queryVector 查询向量
     * @param tenantId 租户ID
     * @param topK 返回结果数量
     * @param threshold 相似度阈值
     * @return 向量文本块列表
     */
    public List<VectorChunk> searchByVector(
            com.pgvector.PGvector queryVector,
            Long tenantId,
            Integer topK,
            Double threshold
    ) {
        return baseMapper.searchByVector(queryVector, tenantId, topK, threshold);
    }

    /**
     * 根据文档ID查询所有文本块
     * 
     * @param documentId 文档ID
     * @return 文本块列表
     */
    public List<VectorChunk> listByDocumentId(Long documentId) {
        return baseMapper.selectByDocumentId(documentId);
    }

    /**
     * 根据文档ID删除所有文本块
     * 
     * @param documentId 文档ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDocumentId(Long documentId) {
        baseMapper.deleteByDocumentId(documentId);
        log.info("删除文档向量成功，文档ID: {}", documentId);
    }

    /**
     * 删除单个向量文本块
     * 
     * @param id 文本块ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteChunk(Long id) {
        removeById(id);
        log.info("删除向量文本块成功，ID: {}", id);
    }

    /**
     * 查询租户下的所有文本块
     * 
     * @param tenantId 租户ID
     * @return 文本块列表
     */
    public List<VectorChunk> listByTenantId(Long tenantId) {
        LambdaQueryWrapper<VectorChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VectorChunk::getTenantId, tenantId);
        wrapper.orderByDesc(VectorChunk::getCreatedAt);
        return list(wrapper);
    }
}