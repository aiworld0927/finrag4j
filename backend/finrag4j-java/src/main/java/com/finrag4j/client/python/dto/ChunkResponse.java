package com.finrag4j.client.python.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本分块响应DTO
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文本分块响应")
public class ChunkResponse {

    @Schema(description = "分块状态：success/failed")
    private String status;

    @Schema(description = "分块结果列表")
    private List<Chunk> chunks;

    @Schema(description = "错误信息")
    private String error;

    /**
     * 文本块
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "文本块")
    public static class Chunk {

        @Schema(description = "块内容")
        private String content;

        @Schema(description = "块序号")
        private Integer index;

        @Schema(description = "块大小")
        private Integer size;
    }
}