package com.finrag4j.search.controller;

import com.finrag4j.search.entity.VectorChunk;
import com.finrag4j.search.service.VectorService;
import com.finrag4j.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 向量管理控制器
 */
@RestController
@RequestMapping("/vector")
@RequiredArgsConstructor
@Tag(name = "向量管理", description = "向量存储、删除、索引管理")
public class VectorController {

    private final VectorService vectorService;

    @PostMapping("/chunk")
    @Operation(summary = "添加向量片段")
    public Result<Void> addChunk(@RequestBody VectorChunk chunk) {
        vectorService.addChunk(chunk);
        return Result.success();
    }

    @PostMapping("/chunk/batch")
    @Operation(summary = "批量添加向量片段")
    public Result<Void> addChunks(@RequestBody List<VectorChunk> chunks) {
        vectorService.addChunks(chunks);
        return Result.success();
    }

    @DeleteMapping("/chunk/{id}")
    @Operation(summary = "删除向量片段")
    public Result<Void> deleteChunk(@PathVariable Long id) {
        vectorService.deleteChunk(id);
        return Result.success();
    }

    @DeleteMapping("/document/{docId}")
    @Operation(summary = "删除文档的所有向量")
    public Result<Void> deleteByDocument(@PathVariable Long docId) {
        vectorService.deleteByDocumentId(docId);
        return Result.success();
    }

    @PostMapping("/rebuild-index")
    @Operation(summary = "重建向量索引")
    public Result<Void> rebuildIndex(@RequestParam Long kbId) {
        vectorService.rebuildIndex(kbId);
        return Result.success();
    }
}
