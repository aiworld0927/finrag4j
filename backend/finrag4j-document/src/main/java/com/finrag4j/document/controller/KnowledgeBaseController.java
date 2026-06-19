package com.finrag4j.document.controller;

import com.finrag4j.document.entity.KnowledgeBase;
import com.finrag4j.document.service.KnowledgeBaseService;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库控制器
 */
@RestController
@RequestMapping("/knowledge-base")
@RequiredArgsConstructor
@Tag(name = "知识库管理", description = "知识库CRUD、文档绑定")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @GetMapping
    @Operation(summary = "查询所有知识库")
    public Result<List<KnowledgeBase>> list() {
        return Result.success(knowledgeBaseService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取知识库详情")
    public Result<KnowledgeBase> getById(@PathVariable Long id) {
        return Result.success(knowledgeBaseService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建知识库")
    public Result<Void> create(@RequestBody @Validated KnowledgeBase kb) {
        knowledgeBaseService.create(kb);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新知识库")
    public Result<Void> update(@PathVariable Long id, @RequestBody KnowledgeBase kb) {
        kb.setId(id);
        knowledgeBaseService.update(kb);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除知识库")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeBaseService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/documents/{docId}")
    @Operation(summary = "绑定文档到知识库")
    public Result<Void> bindDocument(@PathVariable Long id, @PathVariable Long docId) {
        knowledgeBaseService.bindDocument(id, docId);
        return Result.success();
    }

    @DeleteMapping("/{id}/documents/{docId}")
    @Operation(summary = "从知识库解绑文档")
    public Result<Void> unbindDocument(@PathVariable Long id, @PathVariable Long docId) {
        knowledgeBaseService.unbindDocument(id, docId);
        return Result.success();
    }
}
