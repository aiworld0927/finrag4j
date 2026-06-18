package com.finrag4j.client.python.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OCR识别请求DTO
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OCR识别请求")
public class OcrRequest {

    @Schema(description = "图片内容（Base64编码）", required = true)
    @NotBlank(message = "图片内容不能为空")
    private String imageContent;

    @Schema(description = "语言类型：chinese/english/auto")
    @Builder.Default
    private String language = "chinese";
}