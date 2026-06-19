package com.finrag4j.document.service;

import com.finrag4j.document.entity.KbDocument;
import com.finrag4j.document.entity.KnowledgeBase;
import com.finrag4j.document.mapper.KbDocumentMapper;
import com.finrag4j.document.mapper.KnowledgeBaseMapper;
import com.finrag4j.common.BusinessException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库服务
 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> {

    private final KnowledgeBaseMapper kbMapper;
    private final KbDocumentMapper kbDocumentMapper;

    public void create(KnowledgeBase kb) {
        kbMapper.insert(kb);
    }

    public void update(KnowledgeBase kb) {
        kbMapper.updateById(kb);
    }

    public void delete(Long id) {
        // 删除知识库
        kbMapper.deleteById(id);

        // 删除关联关系
        kbDocumentMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KbDocument>()
                        .eq(KbDocument::getKbId, id)
        );
    }

    @Transactional
    public void bindDocument(Long kbId, Long docId) {
        // 检查是否已存在关联
        KbDocument exist = kbDocumentMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KbDocument>()
                        .eq(KbDocument::getKbId, kbId)
                        .eq(KbDocument::getDocumentId, docId)
        );

        if (exist != null) {
            throw new BusinessException(400, "文档已在知识库中");
        }

        KbDocument kbDoc = new KbDocument();
        kbDoc.setKbId(kbId);
        kbDoc.setDocumentId(docId);
        kbDocumentMapper.insert(kbDoc);
    }

    @Transactional
    public void unbindDocument(Long kbId, Long docId) {
        kbDocumentMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KbDocument>()
                        .eq(KbDocument::getKbId, kbId)
                        .eq(KbDocument::getDocumentId, docId)
        );
    }

    public List<KbDocument> getDocuments(Long kbId) {
        return kbDocumentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KbDocument>()
                        .eq(KbDocument::getKbId, kbId)
        );
    }
}
