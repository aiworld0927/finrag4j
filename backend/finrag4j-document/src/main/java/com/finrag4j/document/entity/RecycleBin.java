package com.finrag4j.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 回收站实体
 */
@Data
@TableName("recycle_bin")
public class RecycleBin {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String entityType;  // document, knowledge_base

    private Long entityId;

    private String filePath;

    private Long userId;

    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime deleteTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime expireTime;  // 30天后自动清理
}
