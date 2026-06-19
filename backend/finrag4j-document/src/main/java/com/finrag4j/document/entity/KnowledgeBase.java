package com.finrag4j.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库实体
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String description;

    private String visibility;  // public, private

    private Long userId;

    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
