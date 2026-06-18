package com.finrag4j.config;

import lombok.Data;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ配置类
 * 
 * 功能说明：
 * - 配置RocketMQ生产者和消费者
 * - 支持异步任务消息处理
 * - 支持事务消息
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMQConfig {

    /**
     * NameServer地址
     */
    private String nameServer;

    /**
     * 生产者组名
     */
    private String producerGroup = "finrag4j-producer-group";

    /**
     * 消费者组名
     */
    private String consumerGroup = "finrag4j-consumer-group";

    /**
     * 消息超时时间（毫秒）
     */
    private Integer sendMsgTimeout = 3000;

    /**
     * 消息重试次数
     */
    private Integer retryTimesWhenSendFailed = 2;

    /**
     * 创建RocketMQ模板
     */
    @Bean
    public RocketMQTemplate rocketMQTemplate() {
        RocketMQTemplate template = new RocketMQTemplate();
        template.setNameServer(nameServer);
        template.setProducerGroup(producerGroup);
        return template;
    }
}