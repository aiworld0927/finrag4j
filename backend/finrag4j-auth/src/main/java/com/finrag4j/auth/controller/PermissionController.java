package com.finrag4j.auth.controller;

import com.finrag4j.auth.entity.SysPermission;
import com.finrag4j.auth.service.PermissionService;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限CRUD、菜单管理")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取完整的权限菜单树")
    public Result<List<SysPermission>> getPermissionTree() {
        return Result.success(permissionService.getPermissionTree());
    }

    @GetMapping
    @Operation(summary = "查询所有权限")
    public Result<List<SysPermission>> list() {
        return Result.success(permissionService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取权限详情")
    public Result<SysPermission> getById(@PathVariable Long id) {
        return Result.success(permissionService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建权限")
    public Result<Void> create(@RequestBody @Validated SysPermission permission) {
        permissionService.save(permission);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        permission.setId(id);
        permissionService.updateById(permission);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.removeById(id);
        return Result.success();
    }
}
