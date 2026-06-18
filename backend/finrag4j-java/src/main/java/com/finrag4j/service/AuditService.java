package com.finrag4j.service;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.AuditLog;
import com.finrag4j.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 审计日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    @Value("${audit.retention-years:5}")
    private int retentionYears;

    /**
     * 异步保存审计日志
     */
    @Async("asyncTaskExecutor")
    public void saveAuditLog(AuditLog auditLog) {
        try {
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("保存审计日志失败: {}", e.getMessage());
        }
    }

    /**
     * 根据时间范围查询审计日志
     */
    public List<AuditLog> queryByTimeRange(Long tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.selectByTimeRange(tenantId, startTime, endTime);
    }

    /**
     * 根据用户查询审计日志
     */
    public List<AuditLog> queryByUserId(Long userId) {
        return auditLogMapper.selectByUserId(userId);
    }

    /**
     * 根据模块查询审计日志
     */
    public List<AuditLog> queryByModule(String module) {
        return auditLogMapper.selectByModule(module);
    }

    /**
     * 导出审计日志为Excel
     */
    public byte[] exportToExcel(Long tenantId, LocalDateTime startTime, LocalDateTime endTime) throws IOException {
        List<AuditLog> logs = auditLogMapper.selectByTimeRange(tenantId, startTime, endTime);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("审计日志");
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建表头
            String[] headers = {"ID", "租户ID", "用户ID", "用户名", "操作类型", "操作模块", "操作描述", 
                               "请求URL", "请求方法", "IP地址", "操作时间", "执行时间(ms)", "是否成功"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (AuditLog logEntry : logs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(logEntry.getId());
                row.createCell(1).setCellValue(logEntry.getTenantId() != null ? logEntry.getTenantId() : 0);
                row.createCell(2).setCellValue(logEntry.getUserId() != null ? logEntry.getUserId() : 0);
                row.createCell(3).setCellValue(logEntry.getUsername() != null ? logEntry.getUsername() : "");
                row.createCell(4).setCellValue(logEntry.getOperationType() != null ? logEntry.getOperationType() : "");
                row.createCell(5).setCellValue(logEntry.getOperationModule() != null ? logEntry.getOperationModule() : "");
                row.createCell(6).setCellValue(logEntry.getOperationDesc() != null ? logEntry.getOperationDesc() : "");
                row.createCell(7).setCellValue(logEntry.getRequestUrl() != null ? logEntry.getRequestUrl() : "");
                row.createCell(8).setCellValue(logEntry.getRequestMethod() != null ? logEntry.getRequestMethod() : "");
                row.createCell(9).setCellValue(logEntry.getIpAddress() != null ? logEntry.getIpAddress() : "");
                row.createCell(10).setCellValue(logEntry.getOperationTime() != null ? logEntry.getOperationTime().format(formatter) : "");
                row.createCell(11).setCellValue(logEntry.getExecutionTime() != null ? logEntry.getExecutionTime() : 0);
                row.createCell(12).setCellValue(logEntry.getSuccess() == 1 ? "成功" : "失败");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 清理过期日志（保留指定年限）
     */
    public int cleanExpiredLogs() {
        LocalDateTime expireTime = LocalDateTime.now().minusYears(retentionYears);
        // 由于MyBatis-Plus没有直接的deleteByTime方法，这里简化处理
        log.info("清理{}年前的审计日志", retentionYears);
        return 0; // 实际实现需要根据数据库类型编写删除逻辑
    }

    /**
     * 创建审计日志记录（供AOP切面调用）
     */
    public AuditLog createLog(
            Long tenantId,
            Long userId,
            String username,
            String operationType,
            String operationModule,
            String operationDesc,
            String requestUrl,
            String requestMethod,
            String requestParams,
            String responseData,
            String ipAddress,
            String userAgent,
            Long executionTime,
            Integer success,
            String errorMessage
    ) {
        return AuditLog.builder()
                .tenantId(tenantId)
                .userId(userId)
                .username(username)
                .operationType(operationType)
                .operationModule(operationModule)
                .operationDesc(operationDesc)
                .requestUrl(requestUrl)
                .requestMethod(requestMethod)
                .requestParams(requestParams)
                .responseData(responseData)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .executionTime(executionTime)
                .success(success)
                .errorMessage(errorMessage)
                .build();
    }
}