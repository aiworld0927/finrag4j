package com.finrag4j.client.python.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档解析请求DTO
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档解析请求")
public class ParseRequest {

    @Schema(description = "文件类型：pdf/word/excel/txt", required = true)
    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    @Schema(description = "文件内容（Base64编码）", required = true)
    @NotBlank(message = "文件内容不能为空")
    private String fileContent;

    @Schema(description = "是否需要OCR识别")
    @Builder.Default
    private Boolean needOcr = false;

    @Schema(description = "是否需要文本清洗")
    @Builder.Default
    private Boolean needClean = true;
}