package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.VectorChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 向量文本块Mapper接口
 * 
 * 功能说明：
 * - 向量文本块CRUD操作
 * - 向量相似度检索
 * - 租户隔离查询
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Mapper
public interface VectorChunkMapper extends BaseMapper<VectorChunk> {

    /**
     * 向量相似度检索
     * 
     * @param queryVector 查询向量
     * @param tenantId 租户ID
     * @param topK 返回结果数量
     * @param threshold 相似度阈值
     * @return 向量文本块列表
     */
    @Select("SELECT id, document_id, content, chunk_index, chunk_size, " +
            "1 - (vector <=> #{queryVector}) as similarity " +
            "FROM vector_chunk " +
            "WHERE tenant_id = #{tenantId} AND deleted = 0 " +
            "AND 1 - (vector <=> #{queryVector}) >= #{threshold} " +
            "ORDER BY vector <=> #{queryVector} " +
            "LIMIT #{topK}")
    List<VectorChunk> searchByVector(
            @Param("queryVector") com.pgvector.PGvector queryVector,
            @Param("tenantId") Long tenantId,
            @Param("topK") Integer topK,
            @Param("threshold") Double threshold
    );

    /**
     * 根据文档ID查询所有文本块
     * 
     * @param documentId 文档ID
     * @return 文本块列表
     */
    @Select("SELECT * FROM vector_chunk WHERE document_id = #{documentId} AND deleted = 0 ORDER BY chunk_index")
    List<VectorChunk> selectByDocumentId(@Param("documentId") Long documentId);

    /**
     * 根据文档ID删除所有文本块
     * 
     * @param documentId 文档ID
     */
    @Select("UPDATE vector_chunk SET deleted = 1 WHERE document_id = #{documentId}")
    void deleteByDocumentId(@Param("documentId") Long documentId);
}