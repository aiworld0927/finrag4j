package com.finrag4j.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.Document;
import com.finrag4j.mapper.DocumentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.List;

/**
 * 文档服务
 * 
 * 功能说明：
 * - 文件上传和存储
 * - 文档元数据管理
 * - 文件去重逻辑
 * - 文档CRUD操作
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class DocumentService extends ServiceImpl<DocumentMapper, Document> {

    @Autowired
    private MinioService minioService;

    /**
     * 上传文档
     * 
     * @param file 文件
     * @param tenantId 租户ID
     * @param createdBy 创建人ID
     * @return 文档实体
     */
    @Transactional(rollbackFor = Exception.class)
    public Document uploadDocument(MultipartFile file, Long tenantId, Long createdBy) {
        try {
            // 1. 计算文件MD5
            String fileMd5 = calculateMd5(file);
            
            // 2. 检查文件是否已存在（去重）
            Document existDoc = baseMapper.selectByMd5(fileMd5, tenantId);
            if (existDoc != null) {
                log.info("文件已存在，直接返回: {}", existDoc.getFileName());
                return existDoc;
            }
            
            // 3. 生成存储路径
            String fileType = getFileType(file.getOriginalFilename());
            String storagePath = generateStoragePath(fileType, fileMd5);
            
            // 4. 上传到MinIO
            minioService.uploadFile(file, storagePath);
            
            // 5. 保存文档元数据
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
            
            save(document);
            log.info("文档上传成功: {}", document.getFileName());
            
            return document;
            
        } catch (Exception e) {
            log.error("文档上传失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询文档
     * 
     * @param id 文档ID
     * @return 文档实体
     */
    public Document getDocumentById(Long id) {
        Document document = getById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        return document;
    }

    /**
     * 查询租户下的所有文档
     * 
     * @param tenantId 租户ID
     * @return 文档列表
     */
    public List<Document> listByTenantId(Long tenantId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getTenantId, tenantId);
        wrapper.orderByDesc(Document::getCreatedAt);
        return list(wrapper);
    }

    /**
     * 删除文档
     * 
     * @param id 文档ID
     * @param tenantId 租户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long id, Long tenantId) {
        Document document = getDocumentById(id);
        
        // 租户隔离校验
        if (!document.getTenantId().equals(tenantId)) {
            throw new BusinessException("无权删除该文档");
        }
        
        // 删除MinIO文件
        minioService.deleteFile(document.getStoragePath());
        
        // 删除数据库记录（逻辑删除）
        removeById(id);
        
        log.info("文档删除成功: {}", document.getFileName());
    }

    /**
     * 更新文档状态
     * 
     * @param id 文档ID
     * @param status 状态
     */
    public void updateStatus(Long id, String status) {
        Document document = new Document();
        document.setId(id);
        document.setStatus(status);
        updateById(document);
    }

    /**
     * 更新解析结果
     * 
     * @param id 文档ID
     * @param parsedText 解析后的文本
     * @param pageCount 页数
     */
    public void updateParseResult(Long id, String parsedText, Integer pageCount) {
        Document document = new Document();
        document.setId(id);
        document.setParsedText(parsedText);
        document.setPageCount(pageCount);
        document.setStatus("parsed");
        updateById(document);
    }

    /**
     * 计算文件MD5
     */
    private String calculateMd5(MultipartFile file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(file.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "pdf" -> "pdf";
            case "doc", "docx" -> "word";
            case "xls", "xlsx" -> "excel";
            case "txt" -> "txt";
            default -> "unknown";
        };
    }

    /**
     * 生成存储路径
     */
    private String generateStoragePath(String fileType, String fileMd5) {
        return String.format("%s/%s/%s/%s", 
                "uploads", 
                fileType, 
                fileMd5.substring(0, 2), 
                fileMd5);
    }
}