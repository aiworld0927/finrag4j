package com.finrag4j.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 合规报告实体
 */
@Data
@TableName("compliance_report")
public class ComplianceReport {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long documentId;

    private String checkType;

    private String status;  // pending, processing, completed, failed

    private String riskLevel;  // low, medium, high, critical

    private String findings;  // JSON格式的发现问题

    private String recommendations;  // JSON格式的建议

    private Long userId;

    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
