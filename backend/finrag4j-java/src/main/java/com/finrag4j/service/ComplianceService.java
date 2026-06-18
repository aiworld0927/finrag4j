package com.finrag4j.service;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.SensitiveWord;
import com.finrag4j.mapper.SensitiveWordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 合规规则引擎服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceService {

    private final SensitiveWordMapper sensitiveWordMapper;

    /**
     * 敏感词检测结果
     */
    public record SensitiveResult(
            boolean blocked,
            String message,
            List<String> matchedWords,
            List<Integer> levels
    ) {}

    /**
     * 幻觉检测结果
     */
    public record HallucinationResult(
            boolean blocked,
            String message,
            Double confidence
    ) {}

    /**
     * 检测敏感词
     */
    public SensitiveResult detectSensitiveWords(String text, Long tenantId) {
        List<SensitiveWord> sensitiveWords = sensitiveWordMapper.selectByTenantId(tenantId);
        if (sensitiveWords.isEmpty()) {
            return new SensitiveResult(false, "", Collections.emptyList(), Collections.emptyList());
        }

        List<String> matchedWords = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        boolean blocked = false;

        for (SensitiveWord word : sensitiveWords) {
            if (text.contains(word.getWord())) {
                matchedWords.add(word.getWord());
                levels.add(word.getLevel());
                
                // 级别2的敏感词需要拦截
                if (word.getLevel() == 2) {
                    blocked = true;
                }
            }
        }

        if (blocked) {
            String message = "检测到敏感词，无法继续处理";
            log.warn("敏感词检测拦截: words={}, text={}", matchedWords, truncate(text, 100));
            return new SensitiveResult(true, message, matchedWords, levels);
        }

        if (!matchedWords.isEmpty()) {
            log.info("敏感词检测警告: words={}", matchedWords);
        }

        return new SensitiveResult(false, "", matchedWords, levels);
    }

    /**
     * 检测用户问题中的敏感词
     */
    public void validateUserQuestion(String question, Long tenantId) {
        SensitiveResult result = detectSensitiveWords(question, tenantId);
        if (result.blocked()) {
            throw new BusinessException("您的问题包含敏感内容，无法处理");
        }
    }

    /**
     * 检测AI回答中的敏感词
     */
    public SensitiveResult validateAIAnswer(String answer, Long tenantId) {
        return detectSensitiveWords(answer, tenantId);
    }

    /**
     * 幻觉检测
     */
    public HallucinationResult detectHallucination(String answer, String context, Double similarity) {
        if (similarity == null || similarity < 0.5) {
            return new HallucinationResult(true, "检索相似度过低，无法保证回答准确性", similarity);
        }

        // 检查回答是否包含"不知道"、"无法回答"等明确表示无法回答的内容
        String[] unsurePatterns = {"不知道", "不清楚", "无法回答", "没有相关信息", "查不到"};
        boolean hasUnsure = Arrays.stream(unsurePatterns).anyMatch(answer::contains);
        
        if (hasUnsure && context != null && !context.isEmpty()) {
            // 如果有上下文但AI表示不知道，可能是幻觉
            return new HallucinationResult(true, "回答与上下文不符", similarity);
        }

        // 检查回答长度是否异常（过短或过长）
        if (answer.length() < 10 && context != null && context.length() > 100) {
            return new HallucinationResult(true, "回答过于简略", similarity);
        }

        // 检查是否有编造迹象
        if (containsFabricationIndicators(answer)) {
            return new HallucinationResult(true, "检测到可能的编造内容", similarity);
        }

        return new HallucinationResult(false, "", similarity);
    }

    /**
     * 检查是否包含编造迹象
     */
    private boolean containsFabricationIndicators(String text) {
        // 检测常见的编造模式
        String[] indicators = {
            "根据最新政策",
            "最新规定显示",
            "据内部消息",
            "独家消息",
            "未公开信息",
            "保密信息"
        };
        
        for (String indicator : indicators) {
            if (text.contains(indicator)) {
                log.warn("检测到编造迹象: {}", indicator);
                return true;
            }
        }
        return false;
    }

    /**
     * 低相似度拦截
     */
    public void validateSimilarity(Double similarity, Double threshold) {
        if (similarity == null || similarity < threshold) {
            throw new BusinessException(String.format("检索相似度 %.2f%% 低于阈值 %.2f%%，无法生成回答", 
                    similarity * 100, threshold * 100));
        }
    }

    /**
     * 无知识库内容拦截
     */
    public void validateContext(String context) {
        if (context == null || context.isEmpty()) {
            throw new BusinessException("知识库中没有找到相关内容，无法回答您的问题");
        }
    }

    /**
     * 添加敏感词
     */
    @Transactional
    public SensitiveWord addSensitiveWord(String word, String category, Integer level, Long tenantId) {
        SensitiveWord existing = sensitiveWordMapper.selectByCategory(category, tenantId).stream()
                .filter(w -> w.getWord().equals(word))
                .findFirst()
                .orElse(null);
        
        if (existing != null) {
            throw new BusinessException("敏感词已存在");
        }
        
        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .word(word)
                .category(category != null ? category : "default")
                .level(level != null ? level : 1)
                .tenantId(tenantId)
                .build();
        
        sensitiveWordMapper.insert(sensitiveWord);
        log.info("添加敏感词: {}", word);
        return sensitiveWord;
    }

    /**
     * 删除敏感词
     */
    @Transactional
    public void deleteSensitiveWord(Long id) {
        SensitiveWord word = sensitiveWordMapper.selectById(id);
        if (word == null) {
            throw new BusinessException("敏感词不存在");
        }
        
        word.setDeleted(1);
        sensitiveWordMapper.updateById(word);
        log.info("删除敏感词: {}", word.getWord());
    }

    /**
     * 获取敏感词列表
     */
    public List<SensitiveWord> getSensitiveWords(Long tenantId) {
        return sensitiveWordMapper.selectByTenantId(tenantId);
    }

    /**
     * 检查金融合规性（综合检查）
     */
    public void validateCompliance(
            String question,
            String answer,
            String context,
            Double similarity,
            Double threshold,
            Long tenantId
    ) {
        // 1. 检查用户问题敏感词
        validateUserQuestion(question, tenantId);
        
        // 2. 检查上下文是否为空
        validateContext(context);
        
        // 3. 检查相似度
        validateSimilarity(similarity, threshold);
        
        // 4. 检查AI回答敏感词
        SensitiveResult result = validateAIAnswer(answer, tenantId);
        if (result.blocked()) {
            throw new BusinessException("回答包含敏感内容");
        }
        
        // 5. 幻觉检测
        HallucinationResult hallucinationResult = detectHallucination(answer, context, similarity);
        if (hallucinationResult.blocked()) {
            throw new BusinessException(hallucinationResult.message());
        }
    }

    /**
     * 截断文本
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}