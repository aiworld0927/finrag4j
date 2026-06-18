package com.finrag4j.task.producer;

import com.finrag4j.task.message.DocumentParseMessage;
import com.finrag4j.task.message.VectorIndexMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 任务消息生产者
 * 
 * 功能说明：
 * - 发送文档解析任务消息
 * - 发送向量入库任务消息
 * - 支持延迟消息
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class TaskProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送文档解析任务
     * 
     * @param message 解析消息
     */
    public void sendDocumentParseTask(DocumentParseMessage message) {
        try {
            Message<DocumentParseMessage> rocketMessage = MessageBuilder.withPayload(message).build();
            rocketMQTemplate.syncSend("document-parse-topic", rocketMessage);
            log.info("发送文档解析任务成功，文档ID: {}", message.getDocumentId());
        } catch (Exception e) {
            log.error("发送文档解析任务失败", e);
            throw new RuntimeException("发送文档解析任务失败", e);
        }
    }

    /**
     * 发送向量入库任务
     * 
     * @param message 向量入库消息
     */
    public void sendVectorIndexTask(VectorIndexMessage message) {
        try {
            Message<VectorIndexMessage> rocketMessage = MessageBuilder.withPayload(message).build();
            rocketMQTemplate.syncSend("vector-index-topic", rocketMessage);
            log.info("发送向量入库任务成功，文档ID: {}", message.getDocumentId());
        } catch (Exception e) {
            log.error("发送向量入库任务失败", e);
            throw new RuntimeException("发送向量入库任务失败", e);
        }
    }

    /**
     * 发送延迟消息（用于重试）
     * 
     * @param topic 主题
     * @param message 消息
     * @param delayLevel 延迟级别（1-18，对应1s-2h）
     */
    public void sendDelayMessage(String topic, Object message, int delayLevel) {
        try {
            rocketMQTemplate.syncSend(topic, message, 3000, delayLevel);
            log.info("发送延迟消息成功，主题: {}, 延迟级别: {}", topic, delayLevel);
        } catch (Exception e) {
            log.error("发送延迟消息失败", e);
            throw new RuntimeException("发送延迟消息失败", e);
        }
    }
}