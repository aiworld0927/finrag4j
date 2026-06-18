package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.entity.ComplianceReport;
import com.finrag4j.entity.ExtractRecord;
import com.finrag4j.entity.ExtractTemplate;
import com.finrag4j.entity.RegulationClause;
import com.finrag4j.service.FinanceAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag as ApiTag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 金融Agent业务控制器
 */
@RestController
@RequestMapping("/api/agent")
@ApiTag(name = "金融Agent")
@RequiredArgsConstructor
public class FinanceAgentController {

    private final FinanceAgentService financeAgentService;

    // ==================== 信贷材料抽取Agent ====================

    @Operation(summary = "创建抽取模板")
    @PostMapping("/extract/template")
    public Result<ExtractTemplate> createExtractTemplate(
            @RequestBody ExtractTemplate template,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        template.setTenantId(tenantId);
        ExtractTemplate result = financeAgentService.createExtractTemplate(template);
        return Result.success(result);
    }

    @Operation(summary = "获取抽取模板列表")
    @GetMapping("/extract/templates")
    public Result<List<ExtractTemplate>> getExtractTemplates(@Parameter(description = "租户ID") @RequestParam Long tenantId) {
        List<ExtractTemplate> result = financeAgentService.getExtractTemplates(tenantId);
        return Result.success(result);
    }

    @Operation(summary = "执行信贷材料抽取")
    @PostMapping("/extract/execute")
    public Result<ExtractRecord> extractCreditMaterial(
            @Parameter(description = "文档ID") @RequestParam Long documentId,
            @Parameter(description = "模板ID") @RequestParam Long templateId,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        ExtractRecord result = financeAgentService.extractCreditMaterial(documentId, templateId, tenantId);
        return Result.success(result);
    }

    @Operation(summary = "复核抽取结果")
    @PostMapping("/extract/{recordId}/review")
    public Result<ExtractRecord> reviewExtractRecord(
            @Parameter(description = "记录ID") @PathVariable Long recordId,
            @Parameter(description = "修正结果") @RequestParam String correctedResult,
            @Parameter(description = "备注") @RequestParam(required = false) String comment,
            @Parameter(description = "审核人ID") @RequestParam Long reviewerId
    ) {
        ExtractRecord result = financeAgentService.reviewExtractRecord(recordId, correctedResult, comment, reviewerId);
        return Result.success(result);
    }

    @Operation(summary = "获取抽取记录列表")
    @GetMapping("/extract/records")
    public Result<List<ExtractRecord>> getExtractRecords(
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status
    ) {
        List<ExtractRecord> result = financeAgentService.getExtractRecords(tenantId, status);
        return Result.success(result);
    }

    // ==================== 监管合规自查Agent ====================

    @Operation(summary = "创建合规自查报告")
    @PostMapping("/compliance/report")
    public Result<ComplianceReport> createComplianceReport(
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "报告名称") @RequestParam String reportName,
            @RequestBody List<Long> documentIds
    ) {
        ComplianceReport result = financeAgentService.createComplianceReport(tenantId, reportName, documentIds);
        return Result.success(result);
    }

    @Operation(summary = "执行合规检查")
    @PostMapping("/compliance/check")
    public Result<Void> executeComplianceCheck(
            @Parameter(description = "报告ID") @RequestParam Long reportId,
            @RequestBody List<Long> documentIds,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        financeAgentService.executeComplianceCheck(reportId, documentIds, tenantId);
        return Result.success();
    }

    @Operation(summary = "复核合规报告")
    @PostMapping("/compliance/report/{reportId}/review")
    public Result<ComplianceReport> reviewComplianceReport(
            @Parameter(description = "报告ID") @PathVariable Long reportId,
            @Parameter(description = "审核意见") @RequestParam String comment,
            @Parameter(description = "审核人ID") @RequestParam Long reviewerId
    ) {
        ComplianceReport result = financeAgentService.reviewComplianceReport(reportId, comment, reviewerId);
        return Result.success(result);
    }

    @Operation(summary = "获取合规报告列表")
    @GetMapping("/compliance/reports")
    public Result<List<ComplianceReport>> getComplianceReports(@Parameter(description = "租户ID") @RequestParam Long tenantId) {
        List<ComplianceReport> result = financeAgentService.getComplianceReports(tenantId);
        return Result.success(result);
    }

    // ==================== 制度咨询Agent ====================

    @Operation(summary = "制度咨询问答")
    @PostMapping("/regulation/query")
    public Result<String> queryRegulation(
            @Parameter(description = "问题") @RequestParam String question,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        String result = financeAgentService.queryRegulation(question, tenantId);
        return Result.success(result);
    }

    @Operation(summary = "添加制度条款")
    @PostMapping("/regulation/clause")
    public Result<RegulationClause> addRegulationClause(
            @RequestBody RegulationClause clause,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        clause.setTenantId(tenantId);
        RegulationClause result = financeAgentService.addRegulationClause(clause);
        return Result.success(result);
    }

    @Operation(summary = "获取制度条款列表")
    @GetMapping("/regulation/clauses")
    public Result<List<RegulationClause>> getRegulationClauses(@Parameter(description = "租户ID") @RequestParam Long tenantId) {
        List<RegulationClause> result = financeAgentService.getRegulationClauses(tenantId);
        return Result.success(result);
    }
}