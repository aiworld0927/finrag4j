package com.finrag4j.task.consumer;

import com.finrag4j.client.python.FinDocParseClient;
import com.finrag4j.client.python.dto.ChunkRequest;
import com.finrag4j.client.python.dto.ChunkResponse;
import com.finrag4j.client.python.dto.ParseRequest;
import com.finrag4j.client.python.dto.ParseResponse;
import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.Document;
import com.finrag4j.service.DocumentService;
import com.finrag4j.service.LLMService;
import com.finrag4j.task.message.DocumentParseMessage;
import com.finrag4j.task.message.VectorIndexMessage;
import com.finrag4j.task.producer.TaskProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 文档解析任务消费者
 * 
 * 功能说明：
 * - 消费文档解析任务
 * - 调用Python服务进行解析
 * - 调用Python服务进行分块
 * - 失败重试机制
 * - 解析完成后发送向量入库任务
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "document-parse-topic",
        consumerGroup = "document-parse-consumer-group",
        maxReconsumeTimes = 3
)
public class DocumentParseConsumer implements RocketMQListener<DocumentParseMessage> {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FinDocParseClient finDocParseClient;

    @Autowired
    private LLMService llmService;

    @Autowired
    private TaskProducer taskProducer;

    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public void onMessage(DocumentParseMessage message) {
        Long documentId = message.getDocumentId();
        Long tenantId = message.getTenantId();
        Integer retryCount = message.getRetryCount() != null ? message.getRetryCount() : 0;

        log.info("开始处理文档解析任务，文档ID: {}, 重试次数: {}", documentId, retryCount);

        try {
            // 1. 查询文档
            Document document = documentService.getDocumentById(documentId);
            
            // 租户隔离校验
            if (!document.getTenantId().equals(tenantId)) {
                throw new BusinessException("租户不匹配");
            }

            // 2. 更新文档状态为解析中
            documentService.updateStatus(documentId, "parsing");

            // 3. 调用Python服务解析文档
            ParseRequest parseRequest = ParseRequest.builder()
                    .fileType(document.getFileType())
                    .fileContent("") // TODO: 从MinIO读取文件内容并Base64编码
                    .needOcr(true)
                    .needClean(true)
                    .build();

            ParseResponse parseResponse = finDocParseClient.parseDocument(parseRequest);

            // 4. 更新解析结果
            documentService.updateParseResult(
                    documentId,
                    parseResponse.getText(),
                    parseResponse.getPageCount()
            );

            // 5. 调用Python服务进行文本分块
            ChunkRequest chunkRequest = ChunkRequest.builder()
                    .text(parseResponse.getText())
                    .strategy("regulatory") // TODO: 根据文档类型选择策略
                    .chunkSize(600)
                    .chunkOverlap(100)
                    .build();

            ChunkResponse chunkResponse = finDocParseClient.chunkText(chunkRequest);

            // 6. TODO: 将分块结果存入向量库
            // 这里需要调用向量服务，将文本块向量化后存入PGVector

            // 7. 更新文档状态为已索引
            documentService.updateStatus(documentId, "indexed");

            // 8. 发送向量入库任务
            VectorIndexMessage vectorIndexMessage = VectorIndexMessage.builder()
                    .documentId(documentId)
                    .tenantId(tenantId)
                    .retryCount(0)
                    .build();
            taskProducer.sendVectorIndexTask(vectorIndexMessage);

            log.info("文档解析任务处理成功，文档ID: {}", documentId);

        } catch (Exception e) {
            log.error("文档解析任务处理失败，文档ID: {}, 重试次数: {}", documentId, retryCount, e);

            // 失败重试
            if (retryCount < MAX_RETRY_COUNT) {
                DocumentParseMessage retryMessage = DocumentParseMessage.builder()
                        .documentId(documentId)
                        .tenantId(tenantId)
                        .retryCount(retryCount + 1)
                        .build();

                // 延迟重试（延迟级别5 = 1分钟）
                taskProducer.sendDelayMessage("document-parse-topic", retryMessage, 5);

                log.info("文档解析任务重试，文档ID: {}, 重试次数: {}", documentId, retryCount + 1);
            } else {
                // 超过最大重试次数，标记为失败
                documentService.updateStatus(documentId, "failed");
                log.error("文档解析任务超过最大重试次数，文档ID: {}", documentId);
            }
        }
    }
}