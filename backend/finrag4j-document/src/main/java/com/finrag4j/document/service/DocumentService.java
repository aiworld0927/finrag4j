package com.finrag4j.document.service;

import com.finrag4j.document.client.PythonParseClient;
import com.finrag4j.document.entity.Document;
import com.finrag4j.document.entity.DocumentVersion;
import com.finrag4j.document.entity.RecycleBin;
import com.finrag4j.document.mapper.DocumentMapper;
import com.finrag4j.document.mapper.DocumentVersionMapper;
import com.finrag4j.document.mapper.RecycleBinMapper;
import com.finrag4j.common.BusinessException;
import com.finrag4j.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.util.DigestUtils;

/**
 * 文档服务
 */
@Service
@RequiredArgsConstructor
public class DocumentService extends ServiceImpl<DocumentMapper, Document> {

    private final DocumentMapper documentMapper;
    private final DocumentVersionMapper versionMapper;
    private final RecycleBinMapper recycleBinMapper;
    private final MinioService minioService;
    private final PythonParseClient pythonParseClient;

    /**
     * 上传文档
     */
    @Transactional
    public Document upload(MultipartFile file, Long kbId, List<String> tags) throws Exception {
        // 计算MD5
        String md5 = DigestUtils.md5DigestAsHex(file.getBytes());

        // 检查是否已存在相同MD5的文档
        Document existDoc = documentMapper.selectOne(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getMd5, md5)
                        .eq(Document::getDeleted, 0)
        );

        if (existDoc != null) {
            throw new BusinessException(400, "相同文件已存在: " + existDoc.getName());
        }

        // 上传到MinIO
        String filePath = minioService.upload(file);

        // 创建文档记录
        Document doc = new Document();
        doc.setName(file.getOriginalFilename());
        doc.setFilePath(filePath);
        doc.setFileSize(String.valueOf(file.getSize()));
        doc.setFileType(getFileType(file.getOriginalFilename()));
        doc.setMd5(md5);
        doc.setKbId(kbId);
        doc.setStatus("uploading");
        doc.setTaskId(UUID.randomUUID().toString());
        doc.setUserId(1L); // TODO: 从上下文获取
        doc.setTenantId(1L); // TODO: 从上下文获取
        doc.setVersion(1);

        documentMapper.insert(doc);

        // 触发异步解析任务
        pythonParseClient.submitParseTask(doc.getId(), filePath);

        return doc;
    }

    /**
     * 分页查询文档
     */
    public PageResult<Document> pageQuery(Integer pageNum, Integer pageSize, Long kbId, String status) {
        Page<Document> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();

        if (kbId != null) {
            queryWrapper.eq(Document::getKbId, kbId);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Document::getStatus, status);
        }
        queryWrapper.eq(Document::getDeleted, 0);
        queryWrapper.orderByDesc(Document::getCreateTime);

        Page<Document> result = documentMapper.selectPage(page, queryWrapper);
        return PageResult.of(result.getTotal(), result.getRecords(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 获取文档详情
     */
    public Document getById(Long id) {
        Document doc = documentMapper.selectById(id);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(404, "文档不存在");
        }
        return doc;
    }

    /**
     * 根据TaskId获取文档
     */
    public Document getByTaskId(String taskId) {
        return documentMapper.selectOne(
                new LambdaQueryWrapper<Document>().eq(Document::getTaskId, taskId)
        );
    }

    /**
     * 删除文档到回收站
     */
    @Transactional
    public void delete(Long id) {
        Document doc = getById(id);

        // 移入回收站
        RecycleBin recycleBin = new RecycleBin();
        recycleBin.setEntityType("document");
        recycleBin.setEntityId(id);
        recycleBin.setFilePath(doc.getFilePath());
        recycleBin.setUserId(doc.getUserId());
        recycleBin.setTenantId(doc.getTenantId());
        recycleBin.setExpireTime(LocalDateTime.now().plusDays(30));
        recycleBinMapper.insert(recycleBin);

        // 逻辑删除
        doc.setDeleted(1);
        documentMapper.updateById(doc);
    }

    /**
     * 恢复文档
     */
    @Transactional
    public void recover(Long id) {
        Document doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException(404, "文档不存在");
        }

        doc.setDeleted(0);
        documentMapper.updateById(doc);

        // 删除回收站记录
        recycleBinMapper.delete(
                new LambdaQueryWrapper<RecycleBin>()
                        .eq(RecycleBin::getEntityType, "document")
                        .eq(RecycleBin::getEntityId, id)
        );
    }

    /**
     * 永久删除
     */
    @Transactional
    public void permanentDelete(Long id) {
        Document doc = getById(id);

        // 删除MinIO文件
        minioService.delete(doc.getFilePath());

        // 删除数据库记录
        documentMapper.deleteById(id);

        // 删除回收站记录
        recycleBinMapper.delete(
                new LambdaQueryWrapper<RecycleBin>()
                        .eq(RecycleBin::getEntityType, "document")
                        .eq(RecycleBin::getEntityId, id)
        );
    }

    /**
     * 获取文档版本历史
     */
    public List<Document> getVersions(Long id) {
        Document doc = getById(id);
        List<DocumentVersion> versions = versionMapper.selectList(
                new LambdaQueryWrapper<DocumentVersion>()
                        .eq(DocumentVersion::getDocumentId, id)
                        .orderByDesc(DocumentVersion::getCreateTime)
        );
        // TODO: 转换为Document列表返回
        return null;
    }

    /**
     * 恢复文档到指定版本
     */
    @Transactional
    public void restoreVersion(Long id, Long versionId) {
        // TODO: 实现版本恢复逻辑
    }

    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) return "unknown";
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
