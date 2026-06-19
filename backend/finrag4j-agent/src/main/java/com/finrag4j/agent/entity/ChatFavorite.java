package com.finrag4j.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天收藏实体
 */
@Data
@TableName("chat_favorite")
public class ChatFavorite {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long messageId;

    private String sessionId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
