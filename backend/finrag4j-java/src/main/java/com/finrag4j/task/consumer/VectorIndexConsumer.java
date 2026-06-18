package com.finrag4j.task.consumer;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.Document;
import com.finrag4j.service.DocumentService;
import com.finrag4j.task.message.VectorIndexMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 向量入库任务消费者
 * 
 * 功能说明：
 * - 消费向量入库任务
 * - 将文本块向量化后存入PGVector
 * - 失败重试机制
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "vector-index-topic",
        consumerGroup = "vector-index-consumer-group",
        maxReconsumeTimes = 3
)
public class VectorIndexConsumer implements RocketMQListener<VectorIndexMessage> {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private com.finrag4j.task.producer.TaskProducer taskProducer;

    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public void onMessage(VectorIndexMessage message) {
        Long documentId = message.getDocumentId();
        Long tenantId = message.getTenantId();
        Integer retryCount = message.getRetryCount() != null ? message.getRetryCount() : 0;

        log.info("开始处理向量入库任务，文档ID: {}, 重试次数: {}", documentId, retryCount);

        try {
            // 1. 查询文档
            Document document = documentService.getDocumentById(documentId);
            
            // 租户隔离校验
            if (!document.getTenantId().equals(tenantId)) {
                throw new BusinessException("租户不匹配");
            }

            // 2. TODO: 将解析后的文本分块并向量化
            // 这里需要调用LLM服务的embed方法，将文本块转换为向量
            // 然后调用VectorService批量插入向量

            log.info("向量入库任务处理成功，文档ID: {}", documentId);

        } catch (Exception e) {
            log.error("向量入库任务处理失败，文档ID: {}, 重试次数: {}", documentId, retryCount, e);

            // 失败重试
            if (retryCount < MAX_RETRY_COUNT) {
                VectorIndexMessage retryMessage = VectorIndexMessage.builder()
                        .documentId(documentId)
                        .tenantId(tenantId)
                        .retryCount(retryCount + 1)
                        .build();

                // 延迟重试（延迟级别5 = 1分钟）
                taskProducer.sendDelayMessage("vector-index-topic", retryMessage, 5);

                log.info("向量入库任务重试，文档ID: {}, 重试次数: {}", documentId, retryCount + 1);
            } else {
                // 超过最大重试次数，标记文档为失败
                documentService.updateStatus(documentId, "failed");
                log.error("向量入库任务超过最大重试次数，文档ID: {}", documentId);
            }
        }
    }
}