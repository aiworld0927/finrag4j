package com.finrag4j.service;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.TenantQuota;
import com.finrag4j.mapper.ChatHistoryMapper;
import com.finrag4j.mapper.TenantQuotaMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 模型智能路由服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelRouterService {

    private final TenantQuotaMapper tenantQuotaMapper;
    private final ChatHistoryMapper chatHistoryMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 模型配置
    private static final String SMALL_MODEL = "qwen2:7b";
    private static final String LARGE_MODEL = "qwen2:14b";
    private static final String XL_MODEL = "qwen2:72b";

    // Redis key前缀
    private static final String CONCURRENT_PREFIX = "tenant:concurrent:";
    private static final String DAILY_COUNT_PREFIX = "tenant:daily:";

    /**
     * 路由决策结果
     */
    public record RouteResult(
            String modelName,
            boolean allowed,
            String message,
            Integer remainingRequests
    ) {}

    /**
     * 根据问题复杂度选择模型
     */
    public RouteResult route(String question, Long tenantId) {
        // 1. 检查租户配额
        TenantQuota quota = tenantQuotaMapper.selectByTenantId(tenantId);
        if (quota == null) {
            throw new BusinessException("租户配额未配置");
        }

        // 2. 检查日请求限额
        int todayRequests = getTodayRequests(tenantId);
        if (todayRequests >= quota.getDailyRequestLimit()) {
            return new RouteResult(null, false, "今日请求已达上限", 0);
        }

        // 3. 检查并发限制
        int currentConcurrent = getCurrentConcurrent(tenantId);
        if (currentConcurrent >= quota.getMaxConcurrent()) {
            return new RouteResult(null, false, "当前并发数已达上限", quota.getDailyRequestLimit() - todayRequests);
        }

        // 4. 检查模型白名单
        List<String> allowedModels = parseModelWhitelist(quota.getModelWhitelist());
        
        // 5. 根据问题复杂度选择模型
        String selectedModel = selectModelByComplexity(question, allowedModels);
        
        if (!allowedModels.contains(selectedModel)) {
            selectedModel = allowedModels.isEmpty() ? SMALL_MODEL : allowedModels.get(0);
        }

        log.info("模型路由决策: tenantId={}, model={}, question_length={}", 
                tenantId, selectedModel, question.length());
        
        return new RouteResult(
                selectedModel,
                true,
                "",
                quota.getDailyRequestLimit() - todayRequests - 1
        );
    }

    /**
     * 根据问题复杂度选择模型
     */
    private String selectModelByComplexity(String question, List<String> allowedModels) {
        // 简单问题：短句、日常对话
        if (question.length() < 20) {
            return SMALL_MODEL;
        }

        // 中等问题：一般问答
        if (question.length() < 50) {
            return SMALL_MODEL;
        }

        // 复杂问题：长文本、需要深度推理
        if (question.length() >= 50) {
            // 检查是否包含复杂推理关键词
            String[] complexIndicators = {"分析", "推理", "证明", "推导", "评估", "建议", "方案"};
            boolean isComplex = Arrays.stream(complexIndicators).anyMatch(question::contains);
            
            if (isComplex) {
                if (allowedModels.contains(LARGE_MODEL)) {
                    return LARGE_MODEL;
                }
                if (allowedModels.contains(XL_MODEL)) {
                    return XL_MODEL;
                }
            }
        }

        return SMALL_MODEL;
    }

    /**
     * 解析模型白名单
     */
    private List<String> parseModelWhitelist(String whitelistJson) {
        if (whitelistJson == null || whitelistJson.isEmpty()) {
            return Arrays.asList(SMALL_MODEL, LARGE_MODEL);
        }
        
        try {
            return objectMapper.readValue(whitelistJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析模型白名单失败: {}", e.getMessage());
            return Arrays.asList(SMALL_MODEL, LARGE_MODEL);
        }
    }

    /**
     * 获取今日请求数
     */
    private int getTodayRequests(Long tenantId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = DAILY_COUNT_PREFIX + tenantId + ":" + date;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 增加请求计数
     */
    public void incrementRequestCount(Long tenantId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = DAILY_COUNT_PREFIX + tenantId + ":" + date;
        redisTemplate.opsForValue().increment(key);
        // 24小时过期
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    /**
     * 获取当前并发数
     */
    private int getCurrentConcurrent(Long tenantId) {
        String key = CONCURRENT_PREFIX + tenantId;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 增加并发数
     */
    public void incrementConcurrent(Long tenantId) {
        String key = CONCURRENT_PREFIX + tenantId;
        redisTemplate.opsForValue().increment(key);
    }

    /**
     * 减少并发数
     */
    public void decrementConcurrent(Long tenantId) {
        String key = CONCURRENT_PREFIX + tenantId;
        String value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            int current = Integer.parseInt(value);
            if (current > 0) {
                redisTemplate.opsForValue().decrement(key);
            }
        }
    }

    /**
     * 检查模型是否在白名单中
     */
    public boolean isModelAllowed(String modelName, Long tenantId) {
        TenantQuota quota = tenantQuotaMapper.selectByTenantId(tenantId);
        if (quota == null) {
            return true; // 默认允许所有模型
        }
        
        List<String> allowedModels = parseModelWhitelist(quota.getModelWhitelist());
        return allowedModels.contains(modelName);
    }

    /**
     * 更新租户配额
     */
    public TenantQuota updateQuota(Long tenantId, Integer maxConcurrent, Integer dailyRequestLimit, String modelWhitelist) {
        TenantQuota quota = tenantQuotaMapper.selectByTenantId(tenantId);
        if (quota == null) {
            quota = TenantQuota.builder()
                    .tenantId(tenantId)
                    .maxConcurrent(maxConcurrent != null ? maxConcurrent : 5)
                    .dailyRequestLimit(dailyRequestLimit != null ? dailyRequestLimit : 1000)
                    .modelWhitelist(modelWhitelist)
                    .build();
            tenantQuotaMapper.insert(quota);
        } else {
            if (maxConcurrent != null) {
                quota.setMaxConcurrent(maxConcurrent);
            }
            if (dailyRequestLimit != null) {
                quota.setDailyRequestLimit(dailyRequestLimit);
            }
            if (modelWhitelist != null) {
                quota.setModelWhitelist(modelWhitelist);
            }
            tenantQuotaMapper.updateById(quota);
        }
        
        log.info("更新租户配额: tenantId={}, maxConcurrent={}, dailyRequestLimit={}", 
                tenantId, quota.getMaxConcurrent(), quota.getDailyRequestLimit());
        
        return quota;
    }

    /**
     * 获取租户配额
     */
    public TenantQuota getQuota(Long tenantId) {
        TenantQuota quota = tenantQuotaMapper.selectByTenantId(tenantId);
        if (quota == null) {
            // 返回默认配额
            return TenantQuota.builder()
                    .tenantId(tenantId)
                    .maxConcurrent(5)
                    .dailyRequestLimit(1000)
                    .build();
        }
        return quota;
    }
}