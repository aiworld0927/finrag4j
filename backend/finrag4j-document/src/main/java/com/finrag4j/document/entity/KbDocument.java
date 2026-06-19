package com.finrag4j.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库文档关联实体
 */
@Data
@TableName("kb_document")
public class KbDocument {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long kbId;

    private Long documentId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
