package com.finrag4j.agent.controller;

import com.finrag4j.agent.service.ChatSessionService;
import com.finrag4j.agent.service.RagChatService;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RAG聊天控制器
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "RAG聊天", description = "智能问答、多轮对话、上下文管理")
public class ChatController {

    private final RagChatService ragChatService;
    private final ChatSessionService chatSessionService;

    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "发送消息并获取AI回复（支持RAG）")
    public Result<Map<String, Object>> send(@RequestBody ChatRequest request) {
        return Result.success(ragChatService.chat(request));
    }

    @GetMapping("/history/{sessionId}")
    @Operation(summary = "获取聊天历史")
    public Result<com.finrag4j.common.PageResult<Map<String, Object>>> getHistory(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(chatSessionService.getHistory(sessionId, pageNum, pageSize));
    }

    @PostMapping("/session/create")
    @Operation(summary = "创建会话")
    public Result<Map<String, Object>> createSession(@RequestBody ChatSessionService.CreateSessionRequest request) {
        return Result.success(chatSessionService.createSession(request));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "删除会话")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        chatSessionService.deleteSession(sessionId);
        return Result.success();
    }

    @PostMapping("/session/{sessionId}/favorite/{messageId}")
    @Operation(summary = "收藏消息")
    public Result<Void> favoriteMessage(@PathVariable String sessionId, @PathVariable Long messageId) {
        ragChatService.favoriteMessage(messageId);
        return Result.success();
    }

    @lombok.Data
    public static class ChatRequest {
        private String sessionId;
        private String message;
        private Long kbId;
        private String agentType;  // rag, compliance, extraction
        private Boolean useRerank = true;
    }
}
