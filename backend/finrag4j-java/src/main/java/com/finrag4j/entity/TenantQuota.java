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
 * 租户算力配额实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tenant_quota")
public class TenantQuota {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("max_concurrent")
    private Integer maxConcurrent;

    @TableField("daily_request_limit")
    private Integer dailyRequestLimit;

    @TableField("model_whitelist")
    private String modelWhitelist;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}