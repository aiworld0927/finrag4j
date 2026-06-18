package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.entity.AuditLog;
import com.finrag4j.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag as ApiTag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志控制器
 */
@RestController
@RequestMapping("/api/audit")
@ApiTag(name = "审计日志")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "按时间范围查询审计日志")
    @GetMapping("/time-range")
    public Result<List<AuditLog>> queryByTimeRange(
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        List<AuditLog> result = auditService.queryByTimeRange(tenantId, startTime, endTime);
        return Result.success(result);
    }

    @Operation(summary = "按用户查询审计日志")
    @GetMapping("/user/{userId}")
    public Result<List<AuditLog>> queryByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<AuditLog> result = auditService.queryByUserId(userId);
        return Result.success(result);
    }

    @Operation(summary = "按模块查询审计日志")
    @GetMapping("/module/{module}")
    public Result<List<AuditLog>> queryByModule(@Parameter(description = "模块名") @PathVariable String module) {
        List<AuditLog> result = auditService.queryByModule(module);
        return Result.success(result);
    }

    @Operation(summary = "导出审计日志为Excel")
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) throws IOException {
        byte[] excelData = auditService.exportToExcel(tenantId, startTime, endTime);
        
        String fileName = "审计日志_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    @Operation(summary = "清理过期日志")
    @DeleteMapping("/clean")
    public Result<Integer> cleanExpiredLogs() {
        int count = auditService.cleanExpiredLogs();
        return Result.success(count);
    }
}