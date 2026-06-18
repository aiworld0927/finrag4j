package com.finrag4j.client.python;

import com.finrag4j.client.python.dto.*;
import com.finrag4j.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * Python服务HTTP客户端
 * 
 * 功能说明：
 * - 封装调用Python解析、OCR、分块接口
 * - 支持异常重试、超时处理
 * - 支持负载均衡（多实例）
 * - 统一异常处理和日志记录
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class FinDocParseClient {

    @Autowired
    private PythonServiceConfig config;

    /**
     * 文档解析
     * 
     * @param request 解析请求
     * @return 解析响应
     */
    public ParseResponse parseDocument(ParseRequest request) {
        log.info("调用Python服务进行文档解析，文件类型: {}", request.getFileType());
        
        try {
            ParseResponse response = webClient()
                    .post()
                    .uri("/api/parse")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ParseResponse.class)
                    .timeout(Duration.ofMillis(config.getReadTimeout()))
                    .retryWhen(Retry.backoff(
                            config.getRetryTimes(),
                            Duration.ofMillis(config.getRetryInterval())
                    ))
                    .block();
            
            if (response == null || !"success".equals(response.getStatus())) {
                String errorMsg = response != null ? response.getError() : "解析失败";
                log.error("文档解析失败: {}", errorMsg);
                throw new BusinessException("文档解析失败: " + errorMsg);
            }
            
            log.info("文档解析成功，页数: {}", response.getPageCount());
            return response;
            
        } catch (WebClientResponseException e) {
            log.error("文档解析HTTP异常: {}", e.getMessage());
            throw new BusinessException("文档解析失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文档解析异常: {}", e.getMessage(), e);
            throw new BusinessException("文档解析失败: " + e.getMessage());
        }
    }

    /**
     * OCR识别
     * 
     * @param request OCR请求
     * @return OCR响应
     */
    public OcrResponse recognizeOcr(OcrRequest request) {
        log.info("调用Python服务进行OCR识别，语言: {}", request.getLanguage());
        
        try {
            OcrResponse response = webClient()
                    .post()
                    .uri("/api/ocr")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OcrResponse.class)
                    .timeout(Duration.ofMillis(config.getReadTimeout()))
                    .retryWhen(Retry.backoff(
                            config.getRetryTimes(),
                            Duration.ofMillis(config.getRetryInterval())
                    ))
                    .block();
            
            if (response == null || !"success".equals(response.getStatus())) {
                String errorMsg = response != null ? response.getError() : "OCR识别失败";
                log.error("OCR识别失败: {}", errorMsg);
                throw new BusinessException("OCR识别失败: " + errorMsg);
            }
            
            log.info("OCR识别成功，置信度: {}", response.getConfidence());
            return response;
            
        } catch (WebClientResponseException e) {
            log.error("OCR识别HTTP异常: {}", e.getMessage());
            throw new BusinessException("OCR识别失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("OCR识别异常: {}", e.getMessage(), e);
            throw new BusinessException("OCR识别失败: " + e.getMessage());
        }
    }

    /**
     * 文本分块
     * 
     * @param request 分块请求
     * @return 分块响应
     */
    public ChunkResponse chunkText(ChunkRequest request) {
        log.info("调用Python服务进行文本分块，策略: {}, 分块大小: {}", 
                request.getStrategy(), request.getChunkSize());
        
        try {
            ChunkResponse response = webClient()
                    .post()
                    .uri("/api/chunk")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChunkResponse.class)
                    .timeout(Duration.ofMillis(config.getReadTimeout()))
                    .retryWhen(Retry.backoff(
                            config.getRetryTimes(),
                            Duration.ofMillis(config.getRetryInterval())
                    ))
                    .block();
            
            if (response == null || !"success".equals(response.getStatus())) {
                String errorMsg = response != null ? response.getError() : "文本分块失败";
                log.error("文本分块失败: {}", errorMsg);
                throw new BusinessException("文本分块失败: " + errorMsg);
            }
            
            log.info("文本分块成功，分块数量: {}", 
                    response.getChunks() != null ? response.getChunks().size() : 0);
            return response;
            
        } catch (WebClientResponseException e) {
            log.error("文本分块HTTP异常: {}", e.getMessage());
            throw new BusinessException("文本分块失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文本分块异常: {}", e.getMessage(), e);
            throw new BusinessException("文本分块失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     * 
     * @return 是否健康
     */
    public boolean healthCheck() {
        try {
            String response = webClient()
                    .get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(3000))
                    .block();
            
            return "OK".equals(response);
        } catch (Exception e) {
            log.error("Python服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 创建WebClient
     */
    private WebClient webClient() {
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();
    }
}