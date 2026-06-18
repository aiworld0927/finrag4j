package com.finrag4j.client.python.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OCR识别响应DTO
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OCR识别响应")
public class OcrResponse {

    @Schema(description = "识别状态：success/failed")
    private String status;

    @Schema(description = "识别后的文本内容")
    private String text;

    @Schema(description = "置信度")
    private Double confidence;

    @Schema(description = "错误信息")
    private String error;
}