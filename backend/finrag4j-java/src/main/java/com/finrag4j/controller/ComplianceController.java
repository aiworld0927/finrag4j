package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.entity.SensitiveWord;
import com.finrag4j.entity.TenantQuota;
import com.finrag4j.service.ComplianceService;
import com.finrag4j.service.ModelRouterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag as ApiTag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 合规管理控制器
 */
@RestController
@RequestMapping("/api/compliance")
@ApiTag(name = "合规管理")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceService complianceService;
    private final ModelRouterService modelRouterService;

    // ==================== 敏感词管理 ====================

    @Operation(summary = "添加敏感词")
    @PostMapping("/sensitive-words")
    public Result<SensitiveWord> addSensitiveWord(
            @Parameter(description = "敏感词") @RequestParam String word,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "级别（1-警告，2-拦截）") @RequestParam(defaultValue = "1") Integer level,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        SensitiveWord result = complianceService.addSensitiveWord(word, category, level, tenantId);
        return Result.success(result);
    }

    @Operation(summary = "删除敏感词")
    @DeleteMapping("/sensitive-words/{id}")
    public Result<Void> deleteSensitiveWord(@Parameter(description = "敏感词ID") @PathVariable Long id) {
        complianceService.deleteSensitiveWord(id);
        return Result.success();
    }

    @Operation(summary = "获取敏感词列表")
    @GetMapping("/sensitive-words")
    public Result<List<SensitiveWord>> getSensitiveWords(@Parameter(description = "租户ID") @RequestParam Long tenantId) {
        List<SensitiveWord> result = complianceService.getSensitiveWords(tenantId);
        return Result.success(result);
    }

    @Operation(summary = "检测文本敏感词")
    @PostMapping("/detect")
    public Result<ComplianceService.SensitiveResult> detect(
            @Parameter(description = "文本") @RequestParam String text,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        ComplianceService.SensitiveResult result = complianceService.detectSensitiveWords(text, tenantId);
        return Result.success(result);
    }

    // ==================== 租户配额管理 ====================

    @Operation(summary = "获取租户配额")
    @GetMapping("/quota/{tenantId}")
    public Result<TenantQuota> getQuota(@Parameter(description = "租户ID") @PathVariable Long tenantId) {
        TenantQuota result = modelRouterService.getQuota(tenantId);
        return Result.success(result);
    }

    @Operation(summary = "更新租户配额")
    @PutMapping("/quota/{tenantId}")
    public Result<TenantQuota> updateQuota(
            @Parameter(description = "租户ID") @PathVariable Long tenantId,
            @Parameter(description = "最大并发数") @RequestParam(required = false) Integer maxConcurrent,
            @Parameter(description = "日请求限额") @RequestParam(required = false) Integer dailyRequestLimit,
            @Parameter(description = "模型白名单JSON") @RequestParam(required = false) String modelWhitelist
    ) {
        TenantQuota result = modelRouterService.updateQuota(tenantId, maxConcurrent, dailyRequestLimit, modelWhitelist);
        return Result.success(result);
    }

    @Operation(summary = "检查模型路由")
    @PostMapping("/route")
    public Result<ModelRouterService.RouteResult> checkRoute(
            @Parameter(description = "问题") @RequestParam String question,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        ModelRouterService.RouteResult result = modelRouterService.route(question, tenantId);
        return Result.success(result);
    }
}