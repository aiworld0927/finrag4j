package com.finrag4j.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档版本实体
 */
@Data
@TableName("document_version")
public class DocumentVersion {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long documentId;

    private String filePath;

    private String version;

    private String changeLog;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
