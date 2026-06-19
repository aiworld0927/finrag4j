package com.finrag4j.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roleId;

    private Long permissionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
