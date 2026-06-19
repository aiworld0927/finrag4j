package com.finrag4j.agent.controller;

import com.finrag4j.agent.service.ComplianceService;
import com.finrag4j.agent.service.ExtractionService;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Agent业务控制器
 */
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
@Tag(name = "Agent业务", description = "信贷抽取、合规自查、制度咨询")
public class FinanceAgentController {

    private final ComplianceService complianceService;
    private final ExtractionService extractionService;

    @PostMapping("/compliance/check")
    @Operation(summary = "合规检查", description = "对文档进行合规检查")
    public Result<Map<String, Object>> complianceCheck(@RequestBody ComplianceCheckRequest request) {
        return Result.success(complianceService.check(request));
    }

    @PostMapping("/compliance/report/{reportId}")
    @Operation(summary = "生成合规报告")
    public Result<Map<String, Object>> generateReport(@PathVariable Long reportId) {
        return Result.success(complianceService.generateReport(reportId));
    }

    @GetMapping("/compliance/report/{reportId}")
    @Operation(summary = "获取合规报告")
    public Result<Map<String, Object>> getReport(@PathVariable Long reportId) {
        return Result.success(complianceService.getReport(reportId));
    }

    @PostMapping("/extraction/extract")
    @Operation(summary = "信贷材料抽取", description = "使用模板抽取信贷材料中的关键信息")
    public Result<Map<String, Object>> extract(@RequestBody ExtractionRequest request) {
        return Result.success(extractionService.extract(request));
    }

    @PostMapping("/extraction/template")
    @Operation(summary = "创建抽取模板")
    public Result<Long> createTemplate(@RequestBody Map<String, Object> template) {
        return Result.success(extractionService.createTemplate(template));
    }

    @GetMapping("/extraction/template/{id}")
    @Operation(summary = "获取抽取模板")
    public Result<Map<String, Object>> getTemplate(@PathVariable Long id) {
        return Result.success(extractionService.getTemplate(id));
    }

    @lombok.Data
    public static class ComplianceCheckRequest {
        private Long documentId;
        private String checkType;  // full, quick, custom
    }

    @lombok.Data
    public static class ExtractionRequest {
        private Long documentId;
        private Long templateId;
        private Boolean batchMode;
    }
}
