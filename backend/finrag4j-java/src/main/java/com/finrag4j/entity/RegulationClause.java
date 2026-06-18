package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 制度条款实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("regulation_clause")
public class RegulationClause {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("clause_no")
    private String clauseNo;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("category")
    private String category;

    @TableField("effective_date")
    private LocalDate effectiveDate;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("status")
    private String status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}