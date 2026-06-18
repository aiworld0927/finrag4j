package com.finrag4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.Document;
import com.finrag4j.entity.DocumentVersion;
import com.finrag4j.entity.RecycleBin;
import com.finrag4j.mapper.DocumentMapper;
import com.finrag4j.mapper.DocumentVersionMapper;
import com.finrag4j.mapper.RecycleBinMapper;
import com.finrag4j.task.producer.TaskProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 文档全生命周期服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLifecycleService {

    private final DocumentService documentService;
    private final MinioService minioService;
    private final DocumentVersionMapper documentVersionMapper;
    private final RecycleBinMapper recycleBinMapper;
    private final TaskProducer taskProducer;

    /**
     * 上传文档（带版本管理）
     */
    @Transactional
    public Document uploadDocument(MultipartFile file, Long tenantId, Long createdBy) {
        String fileMd5 = documentService.calculateMd5(file);
        
        // 检查是否已存在相同文件
        Document existingDoc = documentService.getByMd5(fileMd5, tenantId);
        if (existingDoc != null) {
            // 创建新版本
            return createNewVersion(existingDoc, file, createdBy);
        }
        
        // 上传到MinIO
        String fileType = documentService.getFileType(file.getOriginalFilename());
        String storagePath = minioService.uploadFile(file, generateStoragePath(fileType, fileMd5));
        
        // 创建文档
        Document document = Document.builder()
                .fileName(file.getOriginalFilename())
                .fileType(fileType)
                .fileSize(file.getSize())
                .fileMd5(fileMd5)
                .storagePath(storagePath)
                .status("uploaded")
                .tenantId(tenantId)
                .createdBy(createdBy)
                .build();
        
        documentService.save(document);
        
        // 创建初始版本
        createInitialVersion(document, fileMd5, storagePath, createdBy);
        
        // 发送解析任务
        taskProducer.sendDocumentParseTask(document.getId(), tenantId);
        
        log.info("上传文档成功: {}", document.getFileName());
        return document;
    }

    /**
     * 创建初始版本
     */
    private void createInitialVersion(Document document, String fileMd5, String storagePath, Long createdBy) {
        DocumentVersion version = DocumentVersion.builder()
                .documentId(document.getId())
                .versionNumber("v1.0")
                .fileName(document.getFileName())
                .fileMd5(fileMd5)
                .storagePath(storagePath)
                .versionDesc("初始版本")
                .tenantId(document.getTenantId())
                .createdBy(createdBy)
                .build();
        
        documentVersionMapper.insert(version);
    }

    /**
     * 创建新版本
     */
    @Transactional
    public Document createNewVersion(Document document, MultipartFile file, Long createdBy) {
        String fileMd5 = documentService.calculateMd5(file);
        String storagePath = minioService.uploadFile(file, generateStoragePath(document.getFileType(), fileMd5));
        
        // 获取最新版本号
        List<DocumentVersion> versions = documentVersionMapper.selectByDocumentId(document.getId(), document.getTenantId());
        String newVersion = incrementVersion(versions.isEmpty() ? "v1.0" : versions.get(0).getVersionNumber());
        
        DocumentVersion version = DocumentVersion.builder()
                .documentId(document.getId())
                .versionNumber(newVersion)
                .fileName(file.getOriginalFilename())
                .fileMd5(fileMd5)
                .storagePath(storagePath)
                .versionDesc("版本升级")
                .tenantId(document.getTenantId())
                .createdBy(createdBy)
                .build();
        
        documentVersionMapper.insert(version);
        
        // 更新文档最新信息
        document.setFileMd5(fileMd5);
        document.setStoragePath(storagePath);
        document.setStatus("uploaded");
        documentService.updateById(document);
        
        // 发送解析任务
        taskProducer.sendDocumentParseTask(document.getId(), document.getTenantId());
        
        log.info("创建文档新版本成功: {}, version={}", document.getFileName(), newVersion);
        return document;
    }

    /**
     * 版本号递增
     */
    private String incrementVersion(String currentVersion) {
        String numPart = currentVersion.substring(1);
        String[] parts = numPart.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        minor++;
        return String.format("v%d.%d", major, minor);
    }

    /**
     * 生成存储路径
     */
    private String generateStoragePath(String fileType, String fileMd5) {
        String datePath = LocalDateTime.now().toString().substring(0, 10).replace("-", "/");
        return String.format("%s/%s/%s", datePath, fileType, fileMd5);
    }

    /**
     * 获取文档版本列表
     */
    public List<DocumentVersion> getVersions(Long documentId, Long tenantId) {
        return documentVersionMapper.selectByDocumentId(documentId, tenantId);
    }

    /**
     * 删除文档（移到回收站）
     */
    @Transactional
    public void deleteToRecycle(Long documentId, Long deletedBy, Long tenantId) {
        Document document = documentService.getById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        
        // 检查是否已在回收站
        RecycleBin existing = recycleBinMapper.selectByResource("document", documentId, tenantId);
        if (existing != null) {
            throw new BusinessException("文档已在回收站");
        }
        
        // 添加到回收站（30天后过期）
        RecycleBin recycleBin = RecycleBin.builder()
                .resourceType("document")
                .resourceId(documentId)
                .resourceName(document.getFileName())
                .deleteTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(30))
                .deletedBy(deletedBy)
                .tenantId(tenantId)
                .build();
        
        recycleBinMapper.insert(recycleBin);
        
        // 逻辑删除文档
        document.setDeleted(1);
        documentService.updateById(document);
        
        log.info("文档已移到回收站: {}", document.getFileName());
    }

    /**
     * 从回收站恢复文档
     */
    @Transactional
    public void restoreFromRecycle(Long recycleId, Long tenantId) {
        RecycleBin recycleBin = recycleBinMapper.selectById(recycleId);
        if (recycleBin == null) {
            throw new BusinessException("回收站记录不存在");
        }
        
        // 恢复文档
        Document document = documentService.getById(recycleBin.getResourceId());
        if (document != null) {
            document.setDeleted(0);
            documentService.updateById(document);
        }
        
        // 删除回收站记录
        recycleBin.setDeleted(1);
        recycleBinMapper.updateById(recycleBin);
        
        log.info("从回收站恢复文档: {}", recycleBin.getResourceName());
    }

    /**
     * 永久删除文档
     */
    @Transactional
    public void permanentDelete(Long recycleId, Long tenantId) {
        RecycleBin recycleBin = recycleBinMapper.selectById(recycleId);
        if (recycleBin == null) {
            throw new BusinessException("回收站记录不存在");
        }
        
        Long documentId = recycleBin.getResourceId();
        
        // 删除MinIO文件
        Document document = documentService.getById(documentId);
        if (document != null) {
            minioService.deleteFile(document.getStoragePath());
        }
        
        // 删除所有版本
        List<DocumentVersion> versions = documentVersionMapper.selectByDocumentId(documentId, tenantId);
        versions.forEach(v -> {
            minioService.deleteFile(v.getStoragePath());
            documentVersionMapper.deleteById(v.getId());
        });
        
        // 删除文档
        documentService.removeById(documentId);
        
        // 删除回收站记录
        recycleBinMapper.deleteById(recycleId);
        
        log.info("永久删除文档: {}", recycleBin.getResourceName());
    }

    /**
     * 获取回收站列表
     */
    public List<RecycleBin> getRecycleBin(Long tenantId) {
        return recycleBinMapper.selectByTenantId(tenantId);
    }

    /**
     * 预览文档
     */
    public byte[] previewDocument(Long documentId, Long tenantId) {
        Document document = documentService.getById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        
        return minioService.downloadFile(document.getStoragePath());
    }

    /**
     * 获取文档解析文本
     */
    public String getParsedText(Long documentId, Long tenantId) {
        Document document = documentService.getById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        
        if (!"indexed".equals(document.getStatus())) {
            throw new BusinessException("文档尚未解析完成");
        }
        
        return document.getParsedText();
    }
}