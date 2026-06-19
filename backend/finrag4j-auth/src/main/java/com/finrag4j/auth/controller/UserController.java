package com.finrag4j.auth.controller;

import com.finrag4j.auth.entity.SysUser;
import com.finrag4j.auth.service.UserService;
import com.finrag4j.common.PageRequest;
import com.finrag4j.common.PageResult;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户CRUD、状态管理")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页查询用户", description = "支持条件过滤和分页")
    public Result<PageResult<SysUser>> list(@Validated PageRequest request,
                                            @RequestParam(required = false) String username,
                                            @RequestParam(required = false) String status) {
        return Result.success(userService.pageQuery(request, username, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public Result<Void> create(@RequestBody @Validated SysUser user) {
        userService.create(user);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        userService.update(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态", description = "启用/禁用用户")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "分配角色", description = "为用户分配角色")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
}
