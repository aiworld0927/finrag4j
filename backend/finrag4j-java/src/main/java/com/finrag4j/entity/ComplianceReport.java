package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 监管合规自查报告实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("compliance_report")
public class ComplianceReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("report_no")
    private String reportNo;

    @TableField("report_name")
    private String reportName;

    @TableField("status")
    private String status;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("findings_count")
    private Integer findingsCount;

    @TableField("report_content")
    private String reportContent;

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