package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 合规问题明细实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("compliance_finding")
public class ComplianceFinding {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("report_id")
    private Long reportId;

    @TableField("finding_no")
    private String findingNo;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("category")
    private String category;

    @TableField("description")
    private String description;

    @TableField("regulation_ref")
    private String regulationRef;

    @TableField("suggestion")
    private String suggestion;

    @TableField("status")
    private String status;

    @TableField("resolved_by")
    private Long resolvedBy;

    @TableField("resolved_time")
    private LocalDateTime resolvedTime;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}