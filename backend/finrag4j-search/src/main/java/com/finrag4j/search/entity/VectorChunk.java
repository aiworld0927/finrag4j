package com.finrag4j.search.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 向量文本块实体
 */
@Data
@TableName("vector_chunk")
public class VectorChunk {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long documentId;

    private Long kbId;

    private String content;

    private String vector;  // 存储向量数组的JSON

    private Integer chunkIndex;

    private String metadata;  // 额外元数据JSON

    private Long userId;

    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
