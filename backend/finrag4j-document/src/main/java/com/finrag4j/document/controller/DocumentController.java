package com.finrag4j.document.controller;

import com.finrag4j.document.entity.Document;
import com.finrag4j.document.service.DocumentService;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档控制器
 */
@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@Tag(name = "文档管理", description = "文档上传、下载、查询、删除")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @Operation(summary = "上传文档", description = "上传文档到MinIO，并触发异步解析任务")
    public Result<Document> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "kbId", required = false) Long kbId,
                                   @RequestParam(value = "tags", required = false) List<String> tags) {
        return Result.success(documentService.upload(file, kbId, tags));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文档详情")
    public Result<Document> getById(@PathVariable Long id) {
        return Result.success(documentService.getById(id));
    }

    @GetMapping
    @Operation(summary = "分页查询文档")
    public Result<com.finrag4j.common.PageResult<Document>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long kbId,
            @RequestParam(required = false) String status) {
        return Result.success(documentService.pageQuery(pageNum, pageSize, kbId, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档", description = "将文档放入回收站")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/recover")
    @Operation(summary = "恢复文档", description = "从回收站恢复文档")
    public Result<Void> recover(@PathVariable Long id) {
        documentService.recover(id);
        return Result.success();
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "永久删除文档")
    public Result<Void> permanentDelete(@PathVariable Long id) {
        documentService.permanentDelete(id);
        return Result.success();
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "获取文档版本历史")
    public Result<List<Document>> getVersions(@PathVariable Long id) {
        return Result.success(documentService.getVersions(id));
    }

    @PostMapping("/{id}/versions/{versionId}/restore")
    @Operation(summary = "恢复文档版本", description = "将文档恢复到指定版本")
    public Result<Void> restoreVersion(@PathVariable Long id, @PathVariable Long versionId) {
        documentService.restoreVersion(id, versionId);
        return Result.success();
    }

    @GetMapping("/status/{taskId}")
    @Operation(summary = "查询文档处理状态")
    public Result<Document> getStatus(@PathVariable String taskId) {
        return Result.success(documentService.getByTaskId(taskId));
    }
}
