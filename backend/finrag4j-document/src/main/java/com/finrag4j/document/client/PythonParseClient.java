package com.finrag4j.document.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Python文档解析服务客户端
 */
@FeignClient(name = "python-parse-service", url = "${python-service.url}")
public interface PythonParseClient {

    /**
     * 提交文档解析任务
     */
    @PostMapping("/parse/submit")
    void submitParseTask(@RequestParam("docId") Long docId, @RequestParam("filePath") String filePath);

    /**
     * 提交OCR识别任务
     */
    @PostMapping("/ocr/submit")
    void submitOcrTask(@RequestParam("docId") Long docId, @RequestParam("imagePath") String imagePath);
}
