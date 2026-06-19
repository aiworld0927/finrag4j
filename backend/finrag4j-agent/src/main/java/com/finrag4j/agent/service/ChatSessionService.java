package com.finrag4j.agent.service;

import com.finrag4j.agent.entity.ChatHistory;
import com.finrag4j.agent.mapper.ChatHistoryMapper;
import com.finrag4j.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天会话服务
 */
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatHistoryMapper chatHistoryMapper;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 创建会话
     */
    public Map<String, Object> createSession(CreateSessionRequest request) {
        String sessionId = UUID.randomUUID().toString();

        Map<String, Object> session = new HashMap<>();
        session.put("sessionId", sessionId);
        session.put("title", request.getTitle() != null ? request.getTitle() : "新会话");
        session.put("kbId", request.getKbId());
        session.put("agentType", request.getAgentType());
        session.put("createTime", System.currentTimeMillis());

        // 缓存会话信息
        redisTemplate.opsForHash().putAll("chat:session:" + sessionId, session);

        return session;
    }

    /**
     * 获取聊天历史
     */
    public PageResult<Map<String, Object>> getHistory(String sessionId, Integer pageNum, Integer pageSize) {
        Page<ChatHistory> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getSessionId, sessionId)
                .orderByAsc(ChatHistory::getCreateTime);

        Page<ChatHistory> result = chatHistoryMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = result.getRecords().stream()
                .map(h -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", h.getId());
                    map.put("role", h.getRole());
                    map.put("content", h.getContent());
                    map.put("createTime", h.getCreateTime());
                    return map;
                })
                .toList();

        return PageResult.of(result.getTotal(), records, (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) {
        // 删除历史记录
        chatHistoryMapper.delete(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getSessionId, sessionId)
        );

        // 删除会话缓存
        redisTemplate.delete("chat:session:" + sessionId);
        redisTemplate.delete("chat:context:" + sessionId);
    }

    @lombok.Data
    public static class CreateSessionRequest {
        private String title;
        private Long kbId;
        private String agentType;
    }
}
