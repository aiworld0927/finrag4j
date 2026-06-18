package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 向量文本块实体类
 * 
 * 功能说明：
 * - 存储文本块和对应的向量
 * - 支持PGVector向量检索
 * - 租户隔离
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vector_chunk")
@Schema(description = "向量文本块实体")
public class VectorChunk {

    @Schema(description = "文本块ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "文本块内容")
    private String content;

    @Schema(description = "向量（PGVector类型）")
    private com.pgvector.PGvector vector;

    @Schema(description = "块序号")
    private Integer chunkIndex;

    @Schema(description = "块大小")
    private Integer chunkSize;

    @Schema(description = "分块策略：regulatory/contract/notice")
    private String chunkStrategy;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "删除标记：0-未删除 1-已删除")
    @TableLogic
    private Integer deleted;
}