package com.finrag4j.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.KbDocument;
import com.finrag4j.entity.KnowledgeBase;
import com.finrag4j.entity.Tag;
import com.finrag4j.mapper.KbDocumentMapper;
import com.finrag4j.mapper.KnowledgeBaseMapper;
import com.finrag4j.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 知识库管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final TagMapper tagMapper;
    private final KbDocumentMapper kbDocumentMapper;

    /**
     * 创建知识库
     */
    @Transactional
    public KnowledgeBase createKnowledgeBase(KnowledgeBase kb) {
        // 检查编码是否重复
        if (knowledgeBaseMapper.selectByCode(kb.getKbCode()) != null) {
            throw new BusinessException("知识库编码已存在");
        }
        
        kb.setStatus("active");
        if (kb.getSimilarityThreshold() == null) {
            kb.setSimilarityThreshold(0.7);
        }
        if (kb.getTopK() == null) {
            kb.setTopK(5);
        }
        if (kb.getDefaultModel() == null) {
            kb.setDefaultModel("qwen2:7b");
        }
        
        save(kb);
        log.info("创建知识库成功: {}", kb.getKbName());
        return kb;
    }

    /**
     * 更新知识库
     */
    @Transactional
    public KnowledgeBase updateKnowledgeBase(Long id, KnowledgeBase updateKb) {
        KnowledgeBase kb = getById(id);
        if (kb == null) {
            throw new BusinessException("知识库不存在");
        }
        
        // 检查编码是否被其他知识库使用
        KnowledgeBase existing = knowledgeBaseMapper.selectByCode(updateKb.getKbCode());
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("知识库编码已存在");
        }
        
        kb.setKbName(updateKb.getKbName());
        kb.setKbCode(updateKb.getKbCode());
        kb.setDescription(updateKb.getDescription());
        kb.setStatus(updateKb.getStatus());
        kb.setDefaultModel(updateKb.getDefaultModel());
        kb.setSimilarityThreshold(updateKb.getSimilarityThreshold());
        kb.setTopK(updateKb.getTopK());
        
        updateById(kb);
        log.info("更新知识库成功: {}", kb.getKbName());
        return kb;
    }

    /**
     * 删除知识库（逻辑删除）
     */
    @Transactional
    public void deleteKnowledgeBase(Long id, Long tenantId) {
        KnowledgeBase kb = getById(id);
        if (kb == null) {
            throw new BusinessException("知识库不存在");
        }
        
        // 解除所有文档绑定
        kbDocumentMapper.deleteByKbId(id, tenantId);
        
        // 逻辑删除
        kb.setDeleted(1);
        updateById(kb);
        log.info("删除知识库成功: {}", kb.getKbName());
    }

    /**
     * 根据租户ID获取知识库列表
     */
    public List<KnowledgeBase> getByTenantId(Long tenantId) {
        return knowledgeBaseMapper.selectByTenantId(tenantId);
    }

    /**
     * 根据编码获取知识库
     */
    public KnowledgeBase getByCode(String kbCode) {
        return knowledgeBaseMapper.selectByCode(kbCode);
    }

    /**
     * 统计知识库文档数量
     */
    public Integer countDocuments(Long kbId, Long tenantId) {
        return knowledgeBaseMapper.countDocumentsByKbId(kbId, tenantId);
    }

    // ==================== 标签管理 ====================

    /**
     * 创建标签
     */
    @Transactional
    public Tag createTag(Tag tag) {
        if (tagMapper.selectByCode(tag.getTagCode()) != null) {
            throw new BusinessException("标签编码已存在");
        }
        
        tagMapper.insert(tag);
        log.info("创建标签成功: {}", tag.getTagName());
        return tag;
    }

    /**
     * 更新标签
     */
    @Transactional
    public Tag updateTag(Long id, Tag updateTag) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        Tag existing = tagMapper.selectByCode(updateTag.getTagCode());
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("标签编码已存在");
        }
        
        tag.setTagName(updateTag.getTagName());
        tag.setTagCode(updateTag.getTagCode());
        tag.setColor(updateTag.getColor());
        tagMapper.updateById(tag);
        
        log.info("更新标签成功: {}", tag.getTagName());
        return tag;
    }

    /**
     * 删除标签
     */
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        tag.setDeleted(1);
        tagMapper.updateById(tag);
        log.info("删除标签成功: {}", tag.getTagName());
    }

    /**
     * 获取租户标签列表
     */
    public List<Tag> getTagsByTenantId(Long tenantId) {
        return tagMapper.selectByTenantId(tenantId);
    }

    // ==================== 文档绑定 ====================

    /**
     * 绑定文档到知识库
     */
    @Transactional
    public void bindDocument(Long kbId, Long documentId, Long tenantId) {
        // 检查是否已绑定
        List<KbDocument> existing = kbDocumentMapper.selectByKbId(kbId, tenantId);
        boolean alreadyBound = existing.stream()
                .anyMatch(kbd -> kbd.getDocumentId().equals(documentId));
        
        if (alreadyBound) {
            throw new BusinessException("文档已绑定到该知识库");
        }
        
        KbDocument kbDocument = KbDocument.builder()
                .kbId(kbId)
                .documentId(documentId)
                .tenantId(tenantId)
                .build();
        
        kbDocumentMapper.insert(kbDocument);
        log.info("绑定文档到知识库成功: kbId={}, documentId={}", kbId, documentId);
    }

    /**
     * 批量绑定文档到知识库
     */
    @Transactional
    public void bindDocuments(Long kbId, List<Long> documentIds, Long tenantId) {
        for (Long documentId : documentIds) {
            bindDocument(kbId, documentId, tenantId);
        }
    }

    /**
     * 解绑文档
     */
    @Transactional
    public void unbindDocument(Long kbId, Long documentId, Long tenantId) {
        List<KbDocument> kbDocuments = kbDocumentMapper.selectByKbId(kbId, tenantId);
        kbDocuments.stream()
                .filter(kbd -> kbd.getDocumentId().equals(documentId))
                .findFirst()
                .ifPresent(kbd -> kbDocumentMapper.deleteById(kbd.getId()));
        
        log.info("解绑文档成功: kbId={}, documentId={}", kbId, documentId);
    }

    /**
     * 获取知识库绑定的文档ID列表
     */
    public List<Long> getBoundDocumentIds(Long kbId, Long tenantId) {
        List<KbDocument> kbDocuments = kbDocumentMapper.selectByKbId(kbId, tenantId);
        return kbDocuments.stream()
                .map(KbDocument::getDocumentId)
                .toList();
    }
}