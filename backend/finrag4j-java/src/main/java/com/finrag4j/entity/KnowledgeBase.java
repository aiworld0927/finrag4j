package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_base")
public class KnowledgeBase {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("kb_name")
    private String kbName;

    @TableField("kb_code")
    private String kbCode;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status;

    @TableField("default_model")
    private String defaultModel;

    @TableField("similarity_threshold")
    private Double similarityThreshold;

    @TableField("top_k")
    private Integer topK;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}