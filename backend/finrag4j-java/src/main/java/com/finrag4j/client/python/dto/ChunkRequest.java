package com.finrag4j.client.python.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本分块请求DTO
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文本分块请求")
public class ChunkRequest {

    @Schema(description = "待分块的文本内容", required = true)
    @NotBlank(message = "文本内容不能为空")
    private String text;

    @Schema(description = "分块策略：regulatory/contract/notice")
    @Builder.Default
    private String strategy = "regulatory";

    @Schema(description = "分块大小")
    @Builder.Default
    private Integer chunkSize = 600;

    @Schema(description = "分块重叠大小")
    @Builder.Default
    private Integer chunkOverlap = 100;
}