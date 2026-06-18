package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档实体类
 * 
 * 功能说明：
 * - 存储文档元数据信息
 * - 支持文件去重（通过MD5）
 * - 支持版本控制
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("document")
@Schema(description = "文档实体")
public class Document {

    @Schema(description = "文档ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "文档名称")
    private String fileName;

    @Schema(description = "文件类型：pdf/word/excel/txt")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件MD5（用于去重）")
    private String fileMd5;

    @Schema(description = "MinIO存储路径")
    private String storagePath;

    @Schema(description = "文档状态：uploaded/parsed/indexed/failed")
    private String status;

    @Schema(description = "解析后的文本内容")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String parsedText;

    @Schema(description = "页数")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer pageCount;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "创建人ID")
    private Long createdBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Schema(description = "删除标记：0-未删除 1-已删除")
    @TableLogic
    private Integer deleted;
}