package com.finrag4j.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天记录实体
 */
@Data
@TableName("chat_history")
public class ChatHistory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String sessionId;

    private String role;  // user, assistant, system

    private String content;

    private String agentType;  // rag, compliance, extraction

    private Long userId;

    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
