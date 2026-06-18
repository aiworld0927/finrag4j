package com.finrag4j.client.python.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档解析响应DTO
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档解析响应")
public class ParseResponse {

    @Schema(description = "解析状态：success/failed")
    private String status;

    @Schema(description = "解析后的文本内容")
    private String text;

    @Schema(description = "页数")
    private Integer pageCount;

    @Schema(description = "错误信息")
    private String error;

    @Schema(description = "元数据信息")
    private Object metadata;
}