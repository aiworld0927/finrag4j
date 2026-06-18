package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.service.LLMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * 功能说明：
 * - 服务健康检查
 * - 基础功能测试
 * - 环境信息查询
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "健康检查")
public class HealthController {

    @Autowired
    private LLMService llmService;

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "OK");
        data.put("service", "FinRag4j Java主服务");
        data.put("version", "1.0.0");
        data.put("timestamp", System.currentTimeMillis());
        return Result.success(data);
    }

    /**
     * 测试大模型调用
     */
    @Operation(summary = "测试大模型调用")
    @GetMapping("/test/llm")
    public Result<String> testLLM() {
        try {
            String response = llmService.chat("你好，请介绍一下你自己");
            return Result.success(response);
        } catch (Exception e) {
            log.error("大模型调用失败", e);
            return Result.error("大模型调用失败: " + e.getMessage());
        }
    }

    /**
     * 获取环境信息
     */
    @Operation(summary = "获取环境信息")
    @GetMapping("/env")
    public Result<Map<String, String>> getEnvInfo() {
        Map<String, String> env = new HashMap<>();
        env.put("java.version", System.getProperty("java.version"));
        env.put("java.home", System.getProperty("java.home"));
        env.put("os.name", System.getProperty("os.name"));
        env.put("os.arch", System.getProperty("os.arch"));
        env.put("os.version", System.getProperty("os.version"));
        env.put("user.name", System.getProperty("user.name"));
        return Result.success(env);
    }
}