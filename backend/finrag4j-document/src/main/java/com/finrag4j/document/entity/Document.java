package com.finrag4j.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档实体
 */
@Data
@TableName("document")
public class Document {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String filePath;

    private String fileSize;

    private String fileType;

    private String md5;

    private Long kbId;

    private String status;  // uploading, processing, processed, failed

    private String taskId;

    private Long userId;

    private Long tenantId;

    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
