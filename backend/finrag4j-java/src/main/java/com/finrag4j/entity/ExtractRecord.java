package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 信贷材料抽取台账实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("extract_record")
public class ExtractRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("template_id")
    private Long templateId;

    @TableField("document_id")
    private Long documentId;

    @TableField("extract_result")
    private String extractResult;

    @TableField("status")
    private String status;

    @TableField("reviewed_by")
    private Long reviewedBy;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField("review_comment")
    private String reviewComment;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}