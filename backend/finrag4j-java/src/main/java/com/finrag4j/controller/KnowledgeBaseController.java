package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.entity.KnowledgeBase;
import com.finrag4j.entity.Tag;
import com.finrag4j.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag as ApiTag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库管理控制器
 */
@RestController
@RequestMapping("/api/kb")
@ApiTag(name = "知识库管理")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    // ==================== 知识库CRUD ====================

    @Operation(summary = "创建知识库")
    @PostMapping
    public Result<KnowledgeBase> create(
            @RequestBody KnowledgeBase knowledgeBase,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        knowledgeBase.setTenantId(tenantId);
        KnowledgeBase result = knowledgeBaseService.createKnowledgeBase(knowledgeBase);
        return Result.success(result);
    }

    @Operation(summary = "更新知识库")
    @PutMapping("/{id}")
    public Result<KnowledgeBase> update(
            @Parameter(description = "知识库ID") @PathVariable Long id,
            @RequestBody KnowledgeBase knowledgeBase
    ) {
        KnowledgeBase result = knowledgeBaseService.updateKnowledgeBase(id, knowledgeBase);
        return Result.success(result);
    }

    @Operation(summary = "删除知识库")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "知识库ID") @PathVariable Long id,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        knowledgeBaseService.deleteKnowledgeBase(id, tenantId);
        return Result.success();
    }

    @Operation(summary = "获取知识库列表")
    @GetMapping
    public Result<List<KnowledgeBase>> list(
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        List<KnowledgeBase> result = knowledgeBaseService.getByTenantId(tenantId);
        return Result.success(result);
    }

    @Operation(summary = "获取知识库详情")
    @GetMapping("/{id}")
    public Result<KnowledgeBase> getById(@Parameter(description = "知识库ID") @PathVariable Long id) {
        KnowledgeBase result = knowledgeBaseService.getById(id);
        return Result.success(result);
    }

    @Operation(summary = "根据编码获取知识库")
    @GetMapping("/code/{kbCode}")
    public Result<KnowledgeBase> getByCode(@Parameter(description = "知识库编码") @PathVariable String kbCode) {
        KnowledgeBase result = knowledgeBaseService.getByCode(kbCode);
        return Result.success(result);
    }

    // ==================== 标签管理 ====================

    @Operation(summary = "创建标签")
    @PostMapping("/tags")
    public Result<Tag> createTag(
            @RequestBody Tag tag,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        tag.setTenantId(tenantId);
        Tag result = knowledgeBaseService.createTag(tag);
        return Result.success(result);
    }

    @Operation(summary = "更新标签")
    @PutMapping("/tags/{id}")
    public Result<Tag> updateTag(
            @Parameter(description = "标签ID") @PathVariable Long id,
            @RequestBody Tag tag
    ) {
        Tag result = knowledgeBaseService.updateTag(id, tag);
        return Result.success(result);
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/tags/{id}")
    public Result<Void> deleteTag(@Parameter(description = "标签ID") @PathVariable Long id) {
        knowledgeBaseService.deleteTag(id);
        return Result.success();
    }

    @Operation(summary = "获取标签列表")
    @GetMapping("/tags")
    public Result<List<Tag>> listTags(@Parameter(description = "租户ID") @RequestParam Long tenantId) {
        List<Tag> result = knowledgeBaseService.getTagsByTenantId(tenantId);
        return Result.success(result);
    }

    // ==================== 文档绑定 ====================

    @Operation(summary = "绑定文档到知识库")
    @PostMapping("/{kbId}/documents/{documentId}")
    public Result<Void> bindDocument(
            @Parameter(description = "知识库ID") @PathVariable Long kbId,
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        knowledgeBaseService.bindDocument(kbId, documentId, tenantId);
        return Result.success();
    }

    @Operation(summary = "批量绑定文档")
    @PostMapping("/{kbId}/documents/batch")
    public Result<Void> bindDocuments(
            @Parameter(description = "知识库ID") @PathVariable Long kbId,
            @RequestBody List<Long> documentIds,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        knowledgeBaseService.bindDocuments(kbId, documentIds, tenantId);
        return Result.success();
    }

    @Operation(summary = "解绑文档")
    @DeleteMapping("/{kbId}/documents/{documentId}")
    public Result<Void> unbindDocument(
            @Parameter(description = "知识库ID") @PathVariable Long kbId,
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        knowledgeBaseService.unbindDocument(kbId, documentId, tenantId);
        return Result.success();
    }

    @Operation(summary = "获取知识库绑定的文档ID")
    @GetMapping("/{kbId}/documents")
    public Result<List<Long>> getBoundDocuments(
            @Parameter(description = "知识库ID") @PathVariable Long kbId,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        List<Long> result = knowledgeBaseService.getBoundDocumentIds(kbId, tenantId);
        return Result.success(result);
    }
}