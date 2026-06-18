package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租户实体类
 * 
 * @enterprise 商业版多租户功能
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tenant")
public class Tenant {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("tenant_name")
    private String tenantName;

    @TableField("tenant_code")
    private String tenantCode;

    @TableField("status")
    private String status;

    @TableField("max_users")
    private Integer maxUsers;

    @TableField("max_knowledge_bases")
    private Integer maxKnowledgeBases;

    @TableField("daily_request_limit")
    private Integer dailyRequestLimit;

    @TableField("max_concurrent")
    private Integer maxConcurrent;

    @TableField("model_whitelist")
    private String modelWhitelist;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}