package com.finrag4j.task.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文档解析消息
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 重试次数
     */
    private Integer retryCount;
}