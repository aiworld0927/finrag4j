package com.finrag4j.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统权限实体
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long parentId;

    private String name;

    private String code;

    private String type;  // menu, button

    private String path;

    private String icon;

    private Integer sort;

    private String status;  // normal, disabled

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
