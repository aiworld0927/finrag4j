package com.finrag4j.controller;

import com.finrag4j.common.response.Result;
import com.finrag4j.entity.Document;
import com.finrag4j.entity.DocumentVersion;
import com.finrag4j.entity.RecycleBin;
import com.finrag4j.service.DocumentLifecycleService;
import com.finrag4j.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag as ApiTag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文档管理控制器
 */
@RestController
@RequestMapping("/api/documents")
@ApiTag(name = "文档管理")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentLifecycleService documentLifecycleService;
    private final DocumentService documentService;

    @Operation(summary = "上传文档")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Document> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "创建人ID") @RequestParam(required = false) Long createdBy
    ) {
        Document result = documentLifecycleService.uploadDocument(file, tenantId, createdBy);
        return Result.success(result);
    }

    @Operation(summary = "获取文档列表")
    @GetMapping
    public Result<List<Document>> list(
            @Parameter(description = "租户ID") @RequestParam Long tenantId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status
    ) {
        List<Document> result = documentService.getByTenantId(tenantId);
        if (status != null && !status.isEmpty()) {
            result = result.stream().filter(d -> status.equals(d.getStatus())).toList();
        }
        return Result.success(result);
    }

    @Operation(summary = "获取文档详情")
    @GetMapping("/{id}")
    public Result<Document> getById(@Parameter(description = "文档ID") @PathVariable Long id) {
        Document result = documentService.getById(id);
        return Result.success(result);
    }

    @Operation(summary = "更新文档信息")
    @PutMapping("/{id}")
    public Result<Document> update(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @RequestBody Document document
    ) {
        Document result = documentService.updateDocument(id, document);
        return Result.success(result);
    }

    @Operation(summary = "删除文档（移到回收站）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "删除人ID") @RequestParam(required = false) Long deletedBy,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        documentLifecycleService.deleteToRecycle(id, deletedBy, tenantId);
        return Result.success();
    }

    @Operation(summary = "预览文档")
    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> preview(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        byte[] content = documentLifecycleService.previewDocument(id, tenantId);
        Document document = documentService.getById(id);
        
        String fileName = URLEncoder.encode(document.getFileName(), StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @Operation(summary = "下载文档")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        byte[] content = documentLifecycleService.previewDocument(id, tenantId);
        Document document = documentService.getById(id);
        
        String fileName = URLEncoder.encode(document.getFileName(), StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @Operation(summary = "获取文档解析文本")
    @GetMapping("/{id}/parsed-text")
    public Result<String> getParsedText(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        String result = documentLifecycleService.getParsedText(id, tenantId);
        return Result.success(result);
    }

    // ==================== 版本管理 ====================

    @Operation(summary = "获取文档版本列表")
    @GetMapping("/{id}/versions")
    public Result<List<DocumentVersion>> getVersions(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        List<DocumentVersion> result = documentLifecycleService.getVersions(id, tenantId);
        return Result.success(result);
    }

    // ==================== 回收站 ====================

    @Operation(summary = "获取回收站列表")
    @GetMapping("/recycle-bin")
    public Result<List<RecycleBin>> getRecycleBin(@Parameter(description = "租户ID") @RequestParam Long tenantId) {
        List<RecycleBin> result = documentLifecycleService.getRecycleBin(tenantId);
        return Result.success(result);
    }

    @Operation(summary = "从回收站恢复文档")
    @PostMapping("/recycle-bin/{id}/restore")
    public Result<Void> restore(
            @Parameter(description = "回收站记录ID") @PathVariable Long id,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        documentLifecycleService.restoreFromRecycle(id, tenantId);
        return Result.success();
    }

    @Operation(summary = "永久删除文档")
    @DeleteMapping("/recycle-bin/{id}")
    public Result<Void> permanentDelete(
            @Parameter(description = "回收站记录ID") @PathVariable Long id,
            @Parameter(description = "租户ID") @RequestParam Long tenantId
    ) {
        documentLifecycleService.permanentDelete(id, tenantId);
        return Result.success();
    }
}