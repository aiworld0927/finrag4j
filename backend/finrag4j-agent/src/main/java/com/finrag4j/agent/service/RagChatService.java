package com.finrag4j.agent.service;

import com.finrag4j.agent.controller.ChatController.ChatRequest;
import com.finrag4j.agent.entity.ChatHistory;
import com.finrag4j.agent.mapper.ChatHistoryMapper;
import com.finrag4j.agent.mapper.ChatFavoriteMapper;
import com.finrag4j.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * RAG聊天服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final ChatHistoryMapper chatHistoryMapper;
    private final ChatFavoriteMapper chatFavoriteMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final LlmService llmService;

    /**
     * 聊天
     */
    @Transactional
    public Map<String, Object> chat(ChatRequest request) {
        String sessionId = request.getSessionId();

        // 获取历史上下文
        List<ChatHistory> history = chatHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getSessionId, sessionId)
                        .orderByAsc(ChatHistory::getCreateTime)
        );

        // 构建上下文
        String context = buildContext(history);

        // 构建Prompt
        String prompt = buildPrompt(request.getMessage(), context, request.getKbId());

        // 调用LLM
        String response = llmService.chat(prompt);

        // 保存用户消息
        saveMessage(sessionId, "user", request.getMessage(), request.getAgentType());

        // 保存AI回复
        Long messageId = saveMessage(sessionId, "assistant", response, request.getAgentType());

        // 缓存会话上下文
        cacheContext(sessionId, history);

        Map<String, Object> result = new HashMap<>();
        result.put("messageId", messageId);
        result.put("content", response);
        result.put("sessionId", sessionId);

        return result;
    }

    /**
     * 收藏消息
     */
    @Transactional
    public void favoriteMessage(Long messageId) {
        ChatHistory message = chatHistoryMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }

        com.finrag4j.agent.entity.ChatFavorite favorite = new com.finrag4j.agent.entity.ChatFavorite();
        favorite.setMessageId(messageId);
        favorite.setSessionId(message.getSessionId());
        favorite.setUserId(1L); // TODO: 从上下文获取
        chatFavoriteMapper.insert(favorite);
    }

    private String buildContext(List<ChatHistory> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (ChatHistory h : history) {
            sb.append(h.getRole()).append(": ").append(h.getContent()).append("\n");
        }
        return sb.toString();
    }

    private String buildPrompt(String message, String context, Long kbId) {
        // TODO: 调用Search服务获取RAG检索结果
        // 目前是简化实现
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是金融领域的智能助手。\n\n");

        if (!context.isEmpty()) {
            prompt.append("对话历史:\n").append(context).append("\n");
        }

        prompt.append("用户: ").append(message).append("\n");
        prompt.append("助手: ");

        return prompt.toString();
    }

    private Long saveMessage(String sessionId, String role, String content, String agentType) {
        ChatHistory history = new ChatHistory();
        history.setSessionId(sessionId);
        history.setRole(role);
        history.setContent(content);
        history.setAgentType(agentType);
        history.setUserId(1L); // TODO: 从上下文获取
        history.setTenantId(1L); // TODO: 从上下文获取
        chatHistoryMapper.insert(history);
        return history.getId();
    }

    private void cacheContext(String sessionId, List<ChatHistory> history) {
        // 缓存最近10条消息
        int start = Math.max(0, history.size() - 10);
        List<ChatHistory> recentHistory = history.subList(start, history.size());

        StringBuilder sb = new StringBuilder();
        for (ChatHistory h : recentHistory) {
            sb.append(h.getRole()).append(": ").append(h.getContent()).append("\n");
        }

        redisTemplate.opsForValue().set(
                "chat:context:" + sessionId,
                sb.toString()
        );
    }
}
