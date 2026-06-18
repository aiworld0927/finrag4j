package com.finrag4j.service;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.ChatHistory;
import com.finrag4j.entity.KnowledgeBase;
import com.finrag4j.service.ChatSessionService.ChatResponse;
import com.finrag4j.service.ModelRouterService.RouteResult;
import com.finrag4j.service.RagRetrievalService.RagResult;
import com.finrag4j.service.RagRetrievalService.SourceReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * RAG问答核心服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final RagRetrievalService ragRetrievalService;
    private final LLMService llmService;
    private final ChatSessionService chatSessionService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final ComplianceService complianceService;
    private final ModelRouterService modelRouterService;

    /**
     * 问答请求
     */
    public record ChatRequest(
            String sessionId,
            String question,
            Long kbId,
            Long tenantId,
            Long userId
    ) {}

    /**
     * 执行RAG问答
     */
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 获取或创建会话
            String sessionId = request.sessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = chatSessionService.createSession(request.kbId(), request.tenantId(), request.userId());
            }

            // 2. 合规检查 - 用户问题敏感词检测
            complianceService.validateUserQuestion(request.question(), request.tenantId());

            // 3. 模型路由
            RouteResult routeResult = modelRouterService.route(request.question(), request.tenantId());
            if (!routeResult.allowed()) {
                throw new BusinessException(routeResult.message());
            }
            String modelName = routeResult.modelName();

            // 4. 增加并发计数
            modelRouterService.incrementConcurrent(request.tenantId());

            try {
                // 5. RAG检索
                RagResult ragResult = ragRetrievalService.retrieve(
                        request.question(),
                        request.kbId(),
                        request.tenantId()
                );

                // 6. 合规检查 - 上下文验证
                if (ragResult.chunks().isEmpty()) {
                    throw new BusinessException("知识库中没有找到相关内容，无法回答您的问题");
                }

                // 7. 合规检查 - 相似度验证
                KnowledgeBase kb = knowledgeBaseService.getById(request.kbId());
                Double threshold = kb != null && kb.getSimilarityThreshold() != null 
                        ? kb.getSimilarityThreshold() : 0.7;
                complianceService.validateSimilarity(ragResult.avgSimilarity(), threshold);

                // 8. 构建系统提示词（带合规约束）
                String systemPrompt = buildSystemPrompt(ragResult.context());

                // 9. 获取会话历史（多轮对话）
                List<ChatSessionService.ChatMessage> history = chatSessionService.getSessionHistory(sessionId);
                String fullPrompt = buildFullPrompt(systemPrompt, history, request.question());

                // 10. 调用大模型
                String aiAnswer = llmService.chatWithSystem(systemPrompt, request.question());

                // 11. 合规检查 - AI回答检测
                complianceService.validateAIAnswer(aiAnswer, request.tenantId());
                ComplianceService.HallucinationResult hallucinationResult = 
                        complianceService.detectHallucination(aiAnswer, ragResult.context(), ragResult.avgSimilarity());
                if (hallucinationResult.blocked()) {
                    throw new BusinessException(hallucinationResult.message());
                }

                // 12. 添加来源引用到回答
                String answerWithReferences = addReferences(aiAnswer, ragResult.references());

                // 13. 更新会话记忆
                chatSessionService.updateSessionMemory(sessionId, request.question(), answerWithReferences);

                // 14. 保存对话历史到数据库
                long responseTime = System.currentTimeMillis() - startTime;
                ChatHistory chatHistory = chatSessionService.saveChatHistory(
                        sessionId,
                        request.question(),
                        answerWithReferences,
                        ragResult.context(),
                        modelName,
                        ragResult.avgSimilarity(),
                        ragResult.references(),
                        (int) responseTime,
                        request.kbId(),
                        request.tenantId(),
                        request.userId()
                );

                // 15. 增加请求计数
                modelRouterService.incrementRequestCount(request.tenantId());

                log.info("RAG问答完成: sessionId={}, responseTime={}ms, similarity={}", 
                        sessionId, responseTime, ragResult.avgSimilarity());

                return new ChatResponse(
                        sessionId,
                        answerWithReferences,
                        ragResult.references(),
                        ragResult.avgSimilarity(),
                        (int) responseTime,
                        modelName
                );

            } finally {
                // 减少并发计数
                modelRouterService.decrementConcurrent(request.tenantId());
            }

        } catch (BusinessException e) {
            log.warn("RAG问答失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("RAG问答异常: {}", e.getMessage(), e);
            throw new BusinessException("问答服务异常，请稍后重试");
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String context) {
        return """
                你是一个专业的金融知识问答助手。请基于以下参考文档回答用户的问题。
                
                规则：
                1. 必须基于提供的参考文档内容进行回答
                2. 如果参考文档中没有相关信息，请明确说明"知识库中没有找到相关内容"
                3. 不要编造信息或猜测
                4. 回答要准确、简洁
                5. 对于涉及金融敏感内容的问题，请拒绝回答
                
                参考文档：
                %s
                """.formatted(context);
    }

    /**
     * 构建完整提示词（包含历史对话）
     */
    private String buildFullPrompt(String systemPrompt, List<ChatSessionService.ChatMessage> history, String question) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(systemPrompt).append("\n\n");
        
        for (ChatSessionService.ChatMessage msg : history) {
            prompt.append(msg.role()).append(": ").append(msg.content()).append("\n");
        }
        
        prompt.append("user: ").append(question);
        return prompt.toString();
    }

    /**
     * 添加来源引用到回答
     */
    private String addReferences(String answer, List<SourceReference> references) {
        if (references == null || references.isEmpty()) {
            return answer;
        }

        StringBuilder result = new StringBuilder(answer);
        result.append("\n\n---\n\n**参考来源：**\n");
        
        int index = 1;
        for (SourceReference ref : references) {
            result.append(String.format("%d. 《%s》 (相似度: %.1f%%)\n", 
                    index++, ref.fileName(), ref.similarity() * 100));
        }
        
        return result.toString();
    }

    /**
     * 开始新对话
     */
    public ChatResponse startNewChat(String question, Long kbId, Long tenantId, Long userId) {
        // 创建新会话并进行问答
        String sessionId = chatSessionService.createSession(kbId, tenantId, userId);
        return chat(new ChatRequest(sessionId, question, kbId, tenantId, userId));
    }

    /**
     * 继续对话
     */
    public ChatResponse continueChat(String sessionId, String question, Long tenantId, Long userId) {
        // 获取会话信息
        ChatSessionService.SessionMemory memory = chatSessionService.getSessionMemory(sessionId);
        if (memory.kbId() == null) {
            throw new BusinessException("会话不存在或已过期");
        }
        
        return chat(new ChatRequest(sessionId, question, memory.kbId(), tenantId, userId));
    }
}