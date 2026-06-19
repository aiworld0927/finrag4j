package com.finrag4j.agent.service;

import com.finrag4j.agent.controller.FinanceAgentController.ExtractionRequest;
import com.finrag4j.agent.entity.ExtractRecord;
import com.finrag4j.agent.entity.ExtractTemplate;
import com.finrag4j.agent.mapper.ExtractRecordMapper;
import com.finrag4j.agent.mapper.ExtractTemplateMapper;
import com.finrag4j.common.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 信贷材料抽取服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionService {

    private final ExtractRecordMapper extractRecordMapper;
    private final ExtractTemplateMapper extractTemplateMapper;
    private final LlmService llmService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 抽取
     */
    @Transactional
    public Map<String, Object> extract(ExtractionRequest request) {
        log.info("Starting extraction for document: {}, template: {}",
                request.getDocumentId(), request.getTemplateId());

        // 获取模板
        ExtractTemplate template = extractTemplateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new BusinessException(404, "模板不存在");
        }

        // 创建抽取记录
        ExtractRecord record = new ExtractRecord();
        record.setDocumentId(request.getDocumentId());
        record.setTemplateId(request.getTemplateId());
        record.setStatus("processing");
        record.setUserId(1L);
        record.setTenantId(1L);
        extractRecordMapper.insert(record);

        // TODO: 调用Document服务获取文档内容
        // TODO: 根据模板配置调用LLM进行抽取

        // 模拟完成
        record.setStatus("completed");
        record.setExtractionResult("{}");
        extractRecordMapper.updateById(record);

        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getId());
        result.put("status", record.getStatus());
        result.put("extractionResult", record.getExtractionResult());

        return result;
    }

    /**
     * 创建模板
     */
    @Transactional
    public Long createTemplate(Map<String, Object> templateData) {
        ExtractTemplate template = new ExtractTemplate();
        template.setName((String) templateData.get("name"));
        template.setDescription((String) templateData.get("description"));
        template.setTemplateConfig(toJson(templateData.get("config")));
        template.setUserId(1L);
        template.setTenantId(1L);
        extractTemplateMapper.insert(template);
        return template.getId();
    }

    /**
     * 获取模板
     */
    public Map<String, Object> getTemplate(Long id) {
        ExtractTemplate template = extractTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(404, "模板不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", template.getId());
        result.put("name", template.getName());
        result.put("description", template.getDescription());
        result.put("config", template.getTemplateConfig());
        result.put("createTime", template.getCreateTime());

        return result;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
