package com.finrag4j.aspect;

import com.finrag4j.entity.AuditLog;
import com.finrag4j.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志AOP切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    /**
     * 定义切点：所有Controller方法
     */
    @Pointcut("execution(* com.finrag4j.controller..*.*(..))")
    public void controllerMethods() {}

    /**
     * 环绕通知：记录审计日志
     */
    @Around("controllerMethods()")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        
        // 获取请求参数
        String requestParams = extractParams(joinPoint);
        
        // 获取模块名和操作类型
        String module = extractModule(className);
        String operationType = extractOperationType(methodName);
        
        Long tenantId = null;
        Long userId = null;
        String username = null;
        
        // 从请求或上下文中获取租户和用户信息（简化处理）
        if (request != null) {
            try {
                tenantId = request.getHeader("X-Tenant-Id") != null ? 
                        Long.parseLong(request.getHeader("X-Tenant-Id")) : 1L;
                userId = request.getHeader("X-User-Id") != null ? 
                        Long.parseLong(request.getHeader("X-User-Id")) : 1L;
                username = request.getHeader("X-Username");
            } catch (Exception e) {
                tenantId = 1L;
                userId = 1L;
            }
        }
        
        Object result = null;
        int success = 1;
        String errorMessage = null;
        String responseData = null;
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            
            // 记录响应数据（只记录成功情况）
            if (result != null) {
                try {
                    responseData = objectMapper.writeValueAsString(result);
                    // 限制长度
                    if (responseData.length() > 2000) {
                        responseData = responseData.substring(0, 2000) + "...(truncated)";
                    }
                } catch (Exception e) {
                    responseData = "无法序列化响应";
                }
            }
            
            return result;
            
        } catch (Throwable e) {
            success = 0;
            errorMessage = e.getMessage();
            throw e;
            
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 构建并保存审计日志
            AuditLog auditLog = auditService.createLog(
                    tenantId,
                    userId,
                    username,
                    operationType,
                    module,
                    buildOperationDesc(className, methodName),
                    request != null ? request.getRequestURI() : "",
                    request != null ? request.getMethod() : "",
                    requestParams,
                    responseData,
                    request != null ? getClientIp(request) : "",
                    request != null ? request.getHeader("User-Agent") : "",
                    executionTime,
                    success,
                    errorMessage
            );
            
            // 异步保存日志
            auditService.saveAuditLog(auditLog);
            
            log.debug("审计日志已记录: {} - {} - {}ms", module, operationType, executionTime);
        }
    }

    /**
     * 提取请求参数
     */
    private String extractParams(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                Object arg = args[i];
                // 跳过敏感参数和大对象
                if (paramNames[i].toLowerCase().contains("password")) {
                    params.put(paramNames[i], "******");
                } else if (arg != null && !isSimpleType(arg.getClass())) {
                    params.put(paramNames[i], "[complex object]");
                } else {
                    params.put(paramNames[i], arg);
                }
            }
            
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "无法解析参数";
        }
    }

    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() ||
               clazz == String.class ||
               Number.class.isAssignableFrom(clazz) ||
               Boolean.class == clazz ||
               Character.class == clazz;
    }

    /**
     * 从类名提取模块名
     */
    private String extractModule(String className) {
        if (className.contains("KnowledgeBase")) return "knowledge";
        if (className.contains("Document")) return "document";
        if (className.contains("Chat")) return "chat";
        if (className.contains("Compliance")) return "compliance";
        if (className.contains("Workflow")) return "workflow";
        if (className.contains("User") || className.contains("Role")) return "system";
        if (className.contains("Audit")) return "audit";
        if (className.contains("Agent")) return "agent";
        return "other";
    }

    /**
     * 从方法名提取操作类型
     */
    private String extractOperationType(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("add")) return "create";
        if (methodName.startsWith("update") || methodName.startsWith("edit")) return "update";
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) return "delete";
        if (methodName.startsWith("get") || methodName.startsWith("list") || methodName.startsWith("query")) return "query";
        if (methodName.startsWith("upload")) return "upload";
        if (methodName.startsWith("download") || methodName.startsWith("export")) return "download";
        if (methodName.startsWith("login")) return "login";
        if (methodName.startsWith("logout")) return "logout";
        if (methodName.startsWith("chat") || methodName.startsWith("ask")) return "chat";
        return "other";
    }

    /**
     * 构建操作描述
     */
    private String buildOperationDesc(String className, String methodName) {
        return className + "." + methodName;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个IP时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}