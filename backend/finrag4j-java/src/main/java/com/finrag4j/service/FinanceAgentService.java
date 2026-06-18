package com.finrag4j.service;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.*;
import com.finrag4j.mapper.*;
import com.finrag4j.client.python.dto.ParseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 金融Agent业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceAgentService {

    private final ExtractTemplateMapper templateMapper;
    private final ExtractRecordMapper recordMapper;
    private final ComplianceReportMapper reportMapper;
    private final ComplianceFindingMapper findingMapper;
    private final RegulationClauseMapper clauseMapper;
    private final DocumentService documentService;
    private final FinDocParseClient parseClient;
    private final LLMService llmService;
    private final ObjectMapper objectMapper;

    // ==================== 信贷材料抽取Agent ====================

    /**
     * 创建抽取模板
     */
    @Transactional
    public ExtractTemplate createExtractTemplate(ExtractTemplate template) {
        if (templateMapper.selectByCode(template.getTemplateCode()) != null) {
            throw new BusinessException("模板编码已存在");
        }
        templateMapper.insert(template);
        log.info("创建抽取模板: {}", template.getTemplateName());
        return template;
    }

    /**
     * 获取抽取模板列表
     */
    public List<ExtractTemplate> getExtractTemplates(Long tenantId) {
        return templateMapper.selectByTenantId(tenantId);
    }

    /**
     * 执行信贷材料抽取
     */
    @Transactional
    public ExtractRecord extractCreditMaterial(Long documentId, Long templateId, Long tenantId) {
        // 获取文档内容
        Document document = documentService.getById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 获取模板
        ExtractTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException("抽取模板不存在");
        }

        // 调用Python服务解析文档
        ParseResponse parseResponse = parseClient.parseDocument(document.getStoragePath());
        
        // 使用LLM进行结构化抽取
        String extractedResult = extractWithLLM(parseResponse.getContent(), template.getFieldsJson());

        // 创建抽取记录
        ExtractRecord record = ExtractRecord.builder()
                .templateId(templateId)
                .documentId(documentId)
                .extractResult(extractedResult)
                .status("extracted")
                .tenantId(tenantId)
                .build();
        
        recordMapper.insert(record);
        log.info("完成信贷材料抽取: documentId={}, templateId={}", documentId, templateId);
        
        return record;
    }

    /**
     * 使用LLM进行结构化抽取
     */
    private String extractWithLLM(String content, String fieldsJson) {
        try {
            List<Map<String, String>> fields = objectMapper.readValue(fieldsJson, 
                    new TypeReference<List<Map<String, String>>>() {});
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("请从以下文档内容中提取指定字段：\n\n");
            prompt.append("文档内容：\n").append(content).append("\n\n");
            prompt.append("需要提取的字段：\n");
            
            for (Map<String, String> field : fields) {
                prompt.append("- ").append(field.get("name")).append(": ").append(field.get("description")).append("\n");
            }
            
            prompt.append("\n请以JSON格式输出提取结果，键为字段名，值为提取内容。");
            
            String response = llmService.chat(prompt.toString());
            
            return response;
            
        } catch (JsonProcessingException e) {
            log.error("解析字段定义失败: {}", e.getMessage());
            throw new BusinessException("抽取模板格式错误");
        }
    }

    /**
     * 人工复核抽取结果
     */
    @Transactional
    public ExtractRecord reviewExtractRecord(Long recordId, String correctedResult, String comment, Long reviewerId) {
        ExtractRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("抽取记录不存在");
        }
        
        record.setExtractResult(correctedResult);
        record.setStatus("reviewed");
        record.setReviewedBy(reviewerId);
        record.setReviewTime(LocalDateTime.now());
        record.setReviewComment(comment);
        
        recordMapper.updateById(record);
        log.info("完成抽取记录复核: recordId={}", recordId);
        
        return record;
    }

    /**
     * 获取抽取记录列表
     */
    public List<ExtractRecord> getExtractRecords(Long tenantId, String status) {
        return recordMapper.selectByTenantAndStatus(tenantId, status);
    }

    // ==================== 监管合规自查Agent ====================

    /**
     * 创建合规自查报告
     */
    @Transactional
    public ComplianceReport createComplianceReport(Long tenantId, String reportName, List<Long> documentIds) {
        String reportNo = generateReportNo(tenantId);
        
        ComplianceReport report = ComplianceReport.builder()
                .reportNo(reportNo)
                .reportName(reportName)
                .status("draft")
                .findingsCount(0)
                .tenantId(tenantId)
                .build();
        
        reportMapper.insert(report);
        
        // 执行合规检查
        executeComplianceCheck(report.getId(), documentIds, tenantId);
        
        return report;
    }

    /**
     * 执行合规检查
     */
    @Transactional
    public void executeComplianceCheck(Long reportId, List<Long> documentIds, Long tenantId) {
        ComplianceReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }

        List<ComplianceFinding> findings = new ArrayList<>();
        int highRiskCount = 0;
        int mediumRiskCount = 0;
        int lowRiskCount = 0;

        // 获取监管条款
        List<RegulationClause> clauses = clauseMapper.selectByTenantId(tenantId);
        
        for (Long documentId : documentIds) {
            Document document = documentService.getById(documentId);
            if (document == null) continue;

            // 获取文档内容
            String documentContent = getDocumentContent(documentId);
            
            // 匹配监管条款
            for (RegulationClause clause : clauses) {
                String matchResult = matchClause(documentContent, clause);
                if (!matchResult.isEmpty()) {
                    String riskLevel = assessRisk(matchResult);
                    
                    ComplianceFinding finding = ComplianceFinding.builder()
                            .reportId(reportId)
                            .findingNo(generateFindingNo(reportId, findings.size() + 1))
                            .riskLevel(riskLevel)
                            .category(clause.getCategory())
                            .description(matchResult)
                            .regulationRef(clause.getClauseNo() + " - " + clause.getTitle())
                            .suggestion(generateSuggestion(clause))
                            .status("open")
                            .tenantId(tenantId)
                            .build();
                    
                    findings.add(finding);
                    
                    // 统计风险等级
                    switch (riskLevel) {
                        case "high" -> highRiskCount++;
                        case "medium" -> mediumRiskCount++;
                        case "low" -> lowRiskCount++;
                    }
                }
            }
        }

        // 批量插入问题明细
        for (ComplianceFinding finding : findings) {
            findingMapper.insert(finding);
        }

        // 更新报告
        report.setFindingsCount(findings.size());
        report.setRiskLevel(determineOverallRisk(highRiskCount, mediumRiskCount));
        report.setReportContent(generateReportContent(report, findings));
        report.setStatus("reviewing");
        
        reportMapper.updateById(report);
        
        log.info("合规自查完成: reportId={}, findings={}", reportId, findings.size());
    }

    /**
     * 匹配条款
     */
    private String matchClause(String documentContent, RegulationClause clause) {
        // 简化实现：使用关键词匹配
        String[] keywords = clause.getTitle().split("[，,。.\\s]+");
        int matchCount = 0;
        
        for (String keyword : keywords) {
            if (documentContent.contains(keyword)) {
                matchCount++;
            }
        }
        
        if (matchCount >= keywords.length / 2) {
            return "文档内容与监管条款【" + clause.getTitle() + "】相关";
        }
        
        return "";
    }

    /**
     * 风险评估
     */
    private String assessRisk(String matchResult) {
        // 简化风险评估逻辑
        if (matchResult.contains("未按规定") || matchResult.contains("违规")) {
            return "high";
        } else if (matchResult.contains("建议") || matchResult.contains("应")) {
            return "medium";
        } else {
            return "low";
        }
    }

    /**
     * 生成整改建议
     */
    private String generateSuggestion(RegulationClause clause) {
        return "请对照条款【" + clause.getTitle() + "】进行整改，确保符合监管要求。";
    }

    /**
     * 确定整体风险等级
     */
    private String determineOverallRisk(int high, int medium) {
        if (high > 0) return "high";
        if (medium > 0) return "medium";
        return "low";
    }

    /**
     * 生成报告内容
     */
    private String generateReportContent(ComplianceReport report, List<ComplianceFinding> findings) {
        StringBuilder content = new StringBuilder();
        content.append("## 合规自查报告\n\n");
        content.append("报告编号: ").append(report.getReportNo()).append("\n");
        content.append("报告名称: ").append(report.getReportName()).append("\n");
        content.append("风险等级: ").append(report.getRiskLevel()).append("\n");
        content.append("问题数量: ").append(report.getFindingsCount()).append("\n\n");
        
        content.append("## 问题明细\n\n");
        for (ComplianceFinding finding : findings) {
            content.append("- ").append(finding.getFindingNo()).append("\n");
            content.append("  风险等级: ").append(finding.getRiskLevel()).append("\n");
            content.append("  问题描述: ").append(finding.getDescription()).append("\n");
            content.append("  法规依据: ").append(finding.getRegulationRef()).append("\n");
            content.append("  整改建议: ").append(finding.getSuggestion()).append("\n\n");
        }
        
        return content.toString();
    }

    /**
     * 复核合规报告
     */
    @Transactional
    public ComplianceReport reviewComplianceReport(Long reportId, String comment, Long reviewerId) {
        ComplianceReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }
        
        report.setReviewedBy(reviewerId);
        report.setReviewTime(LocalDateTime.now());
        report.setReviewComment(comment);
        report.setStatus("approved");
        
        reportMapper.updateById(report);
        log.info("合规报告已审核: reportId={}", reportId);
        
        return report;
    }

    /**
     * 获取合规报告列表
     */
    public List<ComplianceReport> getComplianceReports(Long tenantId) {
        return reportMapper.selectByTenantId(tenantId);
    }

    // ==================== 制度咨询Agent ====================

    /**
     * 制度咨询问答
     */
    public String queryRegulation(String question, Long tenantId) {
        // 获取相关制度条款
        List<RegulationClause> clauses = searchClauses(question, tenantId);
        
        if (clauses.isEmpty()) {
            return "未找到相关制度条款。";
        }
        
        // 构建上下文
        StringBuilder context = new StringBuilder();
        for (RegulationClause clause : clauses) {
            context.append("条款编号: ").append(clause.getClauseNo()).append("\n");
            context.append("标题: ").append(clause.getTitle()).append("\n");
            context.append("内容: ").append(clause.getContent()).append("\n\n");
        }
        
        // 调用LLM生成回答
        String prompt = """
                请基于以下制度条款回答用户问题：
                
                制度条款：
                %s
                
                用户问题：%s
                
                请给出准确、简洁的回答。
                """.formatted(context.toString(), question);
        
        return llmService.chat(prompt);
    }

    /**
     * 搜索相关条款
     */
    private List<RegulationClause> searchClauses(String question, Long tenantId) {
        List<RegulationClause> allClauses = clauseMapper.selectByTenantId(tenantId);
        
        return allClauses.stream()
                .filter(clause -> {
                    String text = clause.getTitle() + " " + clause.getContent();
                    String[] words = question.split("[，,。.\\s]+");
                    long matchCount = Arrays.stream(words)
                            .filter(word -> text.contains(word))
                            .count();
                    return matchCount >= words.length / 2;
                })
                .collect(Collectors.toList());
    }

    /**
     * 添加制度条款
     */
    @Transactional
    public RegulationClause addRegulationClause(RegulationClause clause) {
        clauseMapper.insert(clause);
        log.info("添加制度条款: {}", clause.getClauseNo());
        return clause;
    }

    /**
     * 获取制度条款列表
     */
    public List<RegulationClause> getRegulationClauses(Long tenantId) {
        return clauseMapper.selectByTenantId(tenantId);
    }

    // ==================== 辅助方法 ====================

    private String getDocumentContent(Long documentId) {
        // 简化实现，实际应从文档服务获取
        return "文档内容";
    }

    private String generateReportNo(Long tenantId) {
        return "CR-" + tenantId + "-" + System.currentTimeMillis();
    }

    private String generateFindingNo(Long reportId, int index) {
        return "F-" + reportId + "-" + String.format("%03d", index);
    }
}