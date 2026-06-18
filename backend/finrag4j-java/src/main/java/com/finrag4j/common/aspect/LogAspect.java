package com.finrag4j.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 日志切面
 * 
 * 功能说明：
 * - 记录接口请求和响应日志
 * - 记录请求参数、响应结果、执行时间
 * - 便于问题排查和性能分析
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 切点：所有controller包下的方法
     */
    @Pointcut("execution(* com.finrag4j..controller..*.*(..))")
    public void logPointCut() {
    }

    /**
     * 环绕通知：记录请求和响应日志
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return point.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = getClientIp(request);
        
        // 记录请求日志
        log.info(">>> 请求开始 - 方法: {}, URI: {}, IP: {}, 参数: {}", 
                method, uri, ip, Arrays.toString(point.getArgs()));
        
        Object result = null;
        try {
            // 执行方法
            result = point.proceed();
            
            // 记录响应日志
            long time = System.currentTimeMillis() - beginTime;
            log.info("<<< 请求结束 - 方法: {}, URI: {}, 耗时: {}ms, 结果: {}", 
                    method, uri, time, result);
            
        } catch (Exception e) {
            // 记录异常日志
            long time = System.currentTimeMillis() - beginTime;
            log.error("!!! 请求异常 - 方法: {}, URI: {}, 耗时: {}ms, 异常: {}", 
                    method, uri, time, e.getMessage(), e);
            throw e;
        }
        
        return result;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}