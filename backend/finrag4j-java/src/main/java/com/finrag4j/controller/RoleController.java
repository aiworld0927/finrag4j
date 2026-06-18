package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.entity.SysPermission;
import com.finrag4j.entity.SysRole;
import com.finrag4j.service.RBACService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag as ApiTag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/roles")
@ApiTag(name = "角色管理")
@RequiredArgsConstructor
public class RoleController {

    private final RBACService rbacService;

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<SysRole> create(
            @RequestBody SysRole role,
            @Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId
    ) {
        role.setTenantId(tenantId);
        SysRole result = rbacService.createRole(role);
        return Result.success(result);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<SysRole> update(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @RequestBody SysRole role
    ) {
        SysRole result = rbacService.updateRole(id, role);
        return Result.success(result);
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "角色ID") @PathVariable Long id) {
        rbacService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "获取角色列表")
    @GetMapping
    public Result<List<SysRole>> list(@Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId) {
        List<SysRole> result = rbacService.getRoles(tenantId);
        return Result.success(result);
    }

    @Operation(summary = "获取角色权限")
    @GetMapping("/{id}/permissions")
    public Result<List<SysPermission>> getPermissions(@Parameter(description = "角色ID") @PathVariable Long id) {
        List<SysPermission> result = rbacService.getRolePermissions(id);
        return Result.success(result);
    }

    @Operation(summary = "分配权限")
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @RequestBody List<Long> permissionIds,
            @Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId
    ) {
        rbacService.assignPermissions(id, permissionIds, tenantId);
        return Result.success();
    }
}