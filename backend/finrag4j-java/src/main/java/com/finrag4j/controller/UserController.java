package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.entity.Department;
import com.finrag4j.entity.SysPermission;
import com.finrag4j.entity.SysRole;
import com.finrag4j.entity.SysUser;
import com.finrag4j.service.RBACService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag as ApiTag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@ApiTag(name = "用户管理")
@RequiredArgsConstructor
public class UserController {

    private final RBACService rbacService;

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<SysUser> create(
            @RequestBody SysUser user,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        user.setTenantId(tenantId);
        SysUser result = rbacService.createUser(user);
        return Result.success(result);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<SysUser> update(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody SysUser user
    ) {
        SysUser result = rbacService.updateUser(id, user);
        return Result.success(result);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        rbacService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "获取用户列表")
    @GetMapping
    public Result<List<SysUser>> list(@Parameter(description = "租户ID") @RequestParam Long tenantId) {
        List<SysUser> result = rbacService.getUsers(tenantId);
        return Result.success(result);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        SysUser user = rbacService.getUserByUsername(""); // 需要实现getById方法
        return Result.success(user);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<SysUser> login(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "密码") @RequestParam String password
    ) {
        SysUser user = rbacService.authenticate(username, password);
        return Result.success(user);
    }

    @Operation(summary = "获取用户角色")
    @GetMapping("/{id}/roles")
    public Result<List<SysRole>> getRoles(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        List<SysRole> result = rbacService.getUserRoles(id, tenantId);
        return Result.success(result);
    }

    @Operation(summary = "分配角色")
    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody List<Long> roleIds,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        rbacService.assignRoles(id, roleIds, tenantId);
        return Result.success();
    }

    @Operation(summary = "获取用户权限")
    @GetMapping("/{id}/permissions")
    public Result<Set<String>> getPermissions(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        Set<String> result = rbacService.getUserPermissions(id, tenantId);
        return Result.success(result);
    }

    @Operation(summary = "检查权限")
    @GetMapping("/{id}/has-permission")
    public Result<Boolean> hasPermission(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "权限编码") @RequestParam String permissionCode,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        boolean result = rbacService.hasPermission(id, permissionCode, tenantId);
        return Result.success(result);
    }
}