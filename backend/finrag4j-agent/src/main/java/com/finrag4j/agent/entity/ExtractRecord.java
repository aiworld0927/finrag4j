package com.finrag4j.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 抽取记录实体
 */
@Data
@TableName("extract_record")
public class ExtractRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long documentId;

    private Long templateId;

    private String extractionResult;  // JSON格式的抽取结果

    private String status;  // pending, completed, failed

    private Long userId;

    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
