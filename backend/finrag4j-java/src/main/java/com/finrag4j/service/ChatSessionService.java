package com.finrag4j.service;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.ChatHistory;
import com.finrag4j.entity.ChatFavorite;
import com.finrag4j.entity.KnowledgeBase;
import com.finrag4j.mapper.ChatFavoriteMapper;
import com.finrag4j.mapper.ChatHistoryMapper;
import com.finrag4j.service.RagRetrievalService.RagResult;
import com.finrag4j.service.RagRetrievalService.SourceReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 多轮对话会话服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final StringRedisTemplate redisTemplate;
    private final ChatHistoryMapper chatHistoryMapper;
    private final ChatFavoriteMapper chatFavoriteMapper;
    private final ObjectMapper objectMapper;

    private static final String SESSION_PREFIX = "chat:session:";
    private static final long SESSION_EXPIRE_HOURS = 24;

    /**
     * 对话响应结果
     */
    public record ChatResponse(
            String sessionId,
            String aiMessage,
            List<SourceReference> references,
            Double similarity,
            Integer responseTime,
            String modelName
    ) {}

    /**
     * 会话记忆
     */
    public record SessionMemory(
            List<ChatMessage> history,
            Long kbId,
            Long tenantId,
            Long userId
    ) {}

    /**
     * 聊天消息
     */
    public record ChatMessage(
            String role,
            String content
    ) {}

    /**
     * 创建新会话
     */
    public String createSession(Long kbId, Long tenantId, Long userId) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        
        SessionMemory memory = new SessionMemory(
                new ArrayList<>(),
                kbId,
                tenantId,
                userId
        );
        
        saveSessionMemory(sessionId, memory);
        log.info("创建新会话: {}", sessionId);
        
        return sessionId;
    }

    /**
     * 获取会话记忆
     */
    public SessionMemory getSessionMemory(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        String json = redisTemplate.opsForValue().get(key);
        
        if (json == null) {
            return new SessionMemory(new ArrayList<>(), null, null, null);
        }
        
        try {
            return objectMapper.readValue(json, SessionMemory.class);
        } catch (JsonProcessingException e) {
            log.error("解析会话记忆失败: {}", e.getMessage());
            return new SessionMemory(new ArrayList<>(), null, null, null);
        }
    }

    /**
     * 保存会话记忆
     */
    private void saveSessionMemory(String sessionId, SessionMemory memory) {
        String key = SESSION_PREFIX + sessionId;
        try {
            String json = objectMapper.writeValueAsString(memory);
            redisTemplate.opsForValue().set(key, json, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("保存会话记忆失败: {}", e.getMessage());
        }
    }

    /**
     * 更新会话记忆（添加消息）
     */
    public void updateSessionMemory(String sessionId, String userMessage, String aiMessage) {
        SessionMemory memory = getSessionMemory(sessionId);
        
        // 限制历史消息数量（最多10轮）
        if (memory.history().size() >= 10) {
            memory = new SessionMemory(
                    new ArrayList<>(memory.history().subList(1, memory.history().size())),
                    memory.kbId(),
                    memory.tenantId(),
                    memory.userId()
            );
        }
        
        List<ChatMessage> newHistory = new ArrayList<>(memory.history());
        newHistory.add(new ChatMessage("user", userMessage));
        newHistory.add(new ChatMessage("assistant", aiMessage));
        
        SessionMemory newMemory = new SessionMemory(
                newHistory,
                memory.kbId(),
                memory.tenantId(),
                memory.userId()
        );
        
        saveSessionMemory(sessionId, newMemory);
    }

    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
        
        // 软删除数据库中的对话历史
        List<ChatHistory> histories = chatHistoryMapper.selectBySessionId(sessionId, 0L);
        histories.forEach(h -> {
            h.setDeleted(1);
            chatHistoryMapper.updateById(h);
        });
        
        log.info("删除会话: {}", sessionId);
    }

    /**
     * 获取会话历史消息
     */
    public List<ChatMessage> getSessionHistory(String sessionId) {
        SessionMemory memory = getSessionMemory(sessionId);
        return memory.history();
    }

    /**
     * 保存对话到数据库
     */
    @Transactional
    public ChatHistory saveChatHistory(
            String sessionId,
            String userMessage,
            String aiMessage,
            String context,
            String modelName,
            Double similarity,
            List<SourceReference> references,
            Integer responseTime,
            Long kbId,
            Long tenantId,
            Long userId
    ) {
        String referencesJson = null;
        try {
            referencesJson = objectMapper.writeValueAsString(references);
        } catch (JsonProcessingException e) {
            log.warn("序列化引用失败: {}", e.getMessage());
        }
        
        ChatHistory chatHistory = ChatHistory.builder()
                .sessionId(sessionId)
                .userMessage(userMessage)
                .aiMessage(aiMessage)
                .context(context)
                .modelName(modelName)
                .similarityAvg(similarity)
                .sourceReferences(referencesJson)
                .responseTime(responseTime)
                .kbId(kbId)
                .tenantId(tenantId)
                .userId(userId)
                .build();
        
        chatHistoryMapper.insert(chatHistory);
        return chatHistory;
    }

    /**
     * 获取用户最近对话记录
     */
    public List<ChatHistory> getRecentChats(Long tenantId, Long userId, Integer limit) {
        return chatHistoryMapper.selectRecentByUserId(tenantId, userId, limit != null ? limit : 10);
    }

    /**
     * 获取会话详细历史
     */
    public List<ChatHistory> getSessionChatHistory(String sessionId, Long tenantId) {
        return chatHistoryMapper.selectBySessionId(sessionId, tenantId);
    }

    // ==================== 收藏功能 ====================

    /**
     * 添加收藏
     */
    @Transactional
    public ChatFavorite addFavorite(String sessionId, Long chatId, String userMessage, String aiMessage, String tags, Long tenantId, Long userId) {
        // 检查是否已收藏
        ChatFavorite existing = chatFavoriteMapper.selectByChatId(chatId, tenantId);
        if (existing != null) {
            throw new BusinessException("该对话已收藏");
        }
        
        ChatFavorite favorite = ChatFavorite.builder()
                .sessionId(sessionId)
                .chatId(chatId)
                .userMessage(userMessage)
                .aiMessage(aiMessage)
                .tags(tags)
                .tenantId(tenantId)
                .userId(userId)
                .build();
        
        chatFavoriteMapper.insert(favorite);
        log.info("添加收藏成功: chatId={}", chatId);
        return favorite;
    }

    /**
     * 删除收藏
     */
    @Transactional
    public void removeFavorite(Long favoriteId) {
        ChatFavorite favorite = chatFavoriteMapper.selectById(favoriteId);
        if (favorite == null) {
            throw new BusinessException("收藏不存在");
        }
        
        favorite.setDeleted(1);
        chatFavoriteMapper.updateById(favorite);
        log.info("删除收藏成功: {}", favoriteId);
    }

    /**
     * 获取用户收藏列表
     */
    public List<ChatFavorite> getFavorites(Long tenantId, Long userId) {
        return chatFavoriteMapper.selectByUserId(tenantId, userId);
    }
}