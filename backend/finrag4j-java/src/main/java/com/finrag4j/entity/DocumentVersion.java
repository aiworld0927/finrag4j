package com.finrag4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档版本实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("document_version")
public class DocumentVersion {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("document_id")
    private Long documentId;

    @TableField("version_number")
    private String versionNumber;

    @TableField("file_name")
    private String fileName;

    @TableField("file_md5")
    private String fileMd5;

    @TableField("storage_path")
    private String storagePath;

    @TableField("parsed_text")
    private String parsedText;

    @TableField("page_count")
    private Integer pageCount;

    @TableField("version_desc")
    private String versionDesc;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("deleted")
    private Integer deleted;
}