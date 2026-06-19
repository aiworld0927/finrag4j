package com.finrag4j.auth.controller;

import com.finrag4j.auth.entity.SysUser;
import com.finrag4j.auth.service.AuthService;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、注册、Token管理")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT Token")
    public Result<Map<String, Object>> login(@RequestBody @Validated LoginRequest request) {
        return Result.success(authService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户")
    public Result<Void> register(@RequestBody @Validated RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "注销当前会话")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用Refresh Token刷新Access Token")
    public Result<Map<String, Object>> refresh(@RequestBody Map<String, String> request) {
        return Result.success(authService.refreshToken(request.get("refreshToken")));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取登录用户详情")
    public Result<SysUser> getCurrentUser(@RequestHeader("Authorization") String token) {
        return Result.success(authService.getCurrentUser(token));
    }

    @lombok.Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @lombok.Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
    }
}
