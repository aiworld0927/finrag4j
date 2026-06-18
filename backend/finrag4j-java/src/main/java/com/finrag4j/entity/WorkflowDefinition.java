package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流定义实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("workflow_definition")
public class WorkflowDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("workflow_name")
    private String workflowName;

    @TableField("workflow_code")
    private String workflowCode;

    @TableField("description")
    private String description;

    @TableField("workflow_json")
    private String workflowJson;

    @TableField("trigger_type")
    private String triggerType;

    @TableField("cron_expression")
    private String cronExpression;

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