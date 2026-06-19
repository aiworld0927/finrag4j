package com.finrag4j.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 抽取模板实体
 */
@Data
@TableName("extract_template")
public class ExtractTemplate {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String description;

    private String templateConfig;  // JSON格式的模板配置

    private Long userId;

    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
