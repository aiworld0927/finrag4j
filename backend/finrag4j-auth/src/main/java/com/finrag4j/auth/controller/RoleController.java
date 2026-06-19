package com.finrag4j.auth.controller;

import com.finrag4j.auth.entity.SysRole;
import com.finrag4j.auth.service.RoleService;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色CRUD、权限分配")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "查询所有角色")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建角色")
    public Result<Void> create(@RequestBody @Validated SysRole role) {
        roleService.create(role);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleService.update(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/permissions")
    @Operation(summary = "分配权限", description = "为角色分配权限")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
}
