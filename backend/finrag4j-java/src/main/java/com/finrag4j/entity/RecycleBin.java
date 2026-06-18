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
 * 回收站实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("recycle_bin")
public class RecycleBin {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("resource_type")
    private String resourceType;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("resource_name")
    private String resourceName;

    @TableField("delete_time")
    private LocalDateTime deleteTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("deleted_by")
    private Long deletedBy;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("deleted")
    private Integer deleted;
}