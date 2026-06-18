package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.entity.ChatFavorite;
import com.finrag4j.entity.ChatHistory;
import com.finrag4j.service.ChatSessionService;
import com.finrag4j.service.RagChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag as ApiTag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 对话管理控制器
 */
@RestController
@RequestMapping("/api/chat")
@ApiTag(name = "RAG问答")
@RequiredArgsConstructor
public class ChatController {

    private final RagChatService ragChatService;
    private final ChatSessionService chatSessionService;

    @Operation(summary = "开始新对话")
    @PostMapping("/new")
    public Result<ChatSessionService.ChatResponse> startNewChat(
            @Parameter(description = "问题") @RequestParam String question,
            @Parameter(description = "知识库ID") @RequestParam Long kbId,
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId
    ) {
        ChatSessionService.ChatResponse result = ragChatService.startNewChat(question, kbId, tenantId, userId);
        return Result.success(result);
    }

    @Operation(summary = "继续对话")
    @PostMapping("/continue")
    public Result<ChatSessionService.ChatResponse> continueChat(
            @Parameter(description = "会话ID") @RequestParam String sessionId,
            @Parameter(description = "问题") @RequestParam String question,
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId
    ) {
        ChatSessionService.ChatResponse result = ragChatService.continueChat(sessionId, question, tenantId, userId);
        return Result.success(result);
    }

    @Operation(summary = "获取会话历史")
    @GetMapping("/history/{sessionId}")
    public Result<List<ChatSessionService.ChatMessage>> getHistory(
            @Parameter(description = "会话ID") @PathVariable String sessionId
    ) {
        List<ChatSessionService.ChatMessage> result = chatSessionService.getSessionHistory(sessionId);
        return Result.success(result);
    }

    @Operation(summary = "获取详细对话历史（含知识库信息）")
    @GetMapping("/history/detail/{sessionId}")
    public Result<List<ChatHistory>> getDetailedHistory(
            @Parameter(description = "会话ID") @PathVariable String sessionId,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        List<ChatHistory> result = chatSessionService.getSessionChatHistory(sessionId, tenantId);
        return Result.success(result);
    }

    @Operation(summary = "结束会话")
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> endSession(@Parameter(description = "会话ID") @PathVariable String sessionId) {
        chatSessionService.deleteSession(sessionId);
        return Result.success();
    }

    @Operation(summary = "获取用户最近对话")
    @GetMapping("/recent")
    public Result<List<ChatHistory>> getRecentChats(
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<ChatHistory> result = chatSessionService.getRecentChats(tenantId, userId, limit);
        return Result.success(result);
    }

    // ==================== 收藏功能 ====================

    @Operation(summary = "添加收藏")
    @PostMapping("/favorite")
    public Result<ChatFavorite> addFavorite(
            @Parameter(description = "会话ID") @RequestParam String sessionId,
            @Parameter(description = "对话ID") @RequestParam Long chatId,
            @Parameter(description = "用户问题") @RequestParam String userMessage,
            @Parameter(description = "AI回答") @RequestParam String aiMessage,
            @Parameter(description = "标签") @RequestParam(required = false) String tags,
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "用户ID") @RequestParam Long userId
    ) {
        ChatFavorite result = chatSessionService.addFavorite(sessionId, chatId, userMessage, aiMessage, tags, tenantId, userId);
        return Result.success(result);
    }

    @Operation(summary = "删除收藏")
    @DeleteMapping("/favorite/{id}")
    public Result<Void> removeFavorite(@Parameter(description = "收藏ID") @PathVariable Long id) {
        chatSessionService.removeFavorite(id);
        return Result.success();
    }

    @Operation(summary = "获取用户收藏列表")
    @GetMapping("/favorites")
    public Result<List<ChatFavorite>> getFavorites(
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "用户ID") @RequestParam Long userId
    ) {
        List<ChatFavorite> result = chatSessionService.getFavorites(tenantId, userId);
        return Result.success(result);
    }
}