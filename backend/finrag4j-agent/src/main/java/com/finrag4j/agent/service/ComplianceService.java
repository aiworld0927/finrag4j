package com.finrag4j.agent.service;

import com.finrag4j.agent.controller.FinanceAgentController.ComplianceCheckRequest;
import com.finrag4j.agent.entity.ComplianceReport;
import com.finrag4j.agent.mapper.ComplianceReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 合规检查服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final ComplianceReportMapper complianceReportMapper;
    private final LlmService llmService;

    /**
     * 执行合规检查
     */
    @Transactional
    public Map<String, Object> check(ComplianceCheckRequest request) {
        log.info("Starting compliance check for document: {}", request.getDocumentId());

        // 创建检查报告
        ComplianceReport report = new ComplianceReport();
        report.setDocumentId(request.getDocumentId());
        report.setCheckType(request.getCheckType());
        report.setStatus("processing");
        report.setUserId(1L); // TODO: 从上下文获取
        report.setTenantId(1L);
        complianceReportMapper.insert(report);

        // TODO: 调用Document服务获取文档内容
        // TODO: 调用Search服务获取相关法规
        // TODO: 使用LLM进行合规分析

        // 模拟完成
        report.setStatus("completed");
        report.setRiskLevel("medium");
        report.setFindings("[]");
        report.setRecommendations("[]");
        complianceReportMapper.updateById(report);

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.getId());
        result.put("status", report.getStatus());
        result.put("riskLevel", report.getRiskLevel());

        return result;
    }

    /**
     * 生成合规报告
     */
    public Map<String, Object> generateReport(Long reportId) {
        ComplianceReport report = complianceReportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found");
        }

        // TODO: 使用LLM生成详细报告内容
        String reportContent = llmService.chat("请生成一份详细的合规报告...");

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.getId());
        result.put("content", reportContent);
        result.put("riskLevel", report.getRiskLevel());
        result.put("findings", report.getFindings());
        result.put("recommendations", report.getRecommendations());

        return result;
    }

    /**
     * 获取报告
     */
    public Map<String, Object> getReport(Long reportId) {
        ComplianceReport report = complianceReportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.getId());
        result.put("documentId", report.getDocumentId());
        result.put("checkType", report.getCheckType());
        result.put("status", report.getStatus());
        result.put("riskLevel", report.getRiskLevel());
        result.put("findings", report.getFindings());
        result.put("recommendations", report.getRecommendations());
        result.put("createTime", report.getCreateTime());

        return result;
    }
}
