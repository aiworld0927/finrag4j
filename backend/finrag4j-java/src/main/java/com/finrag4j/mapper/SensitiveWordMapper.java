package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 敏感词Mapper接口
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    @Select("SELECT sw.* FROM sensitive_word sw WHERE sw.tenant_id = #{tenantId} AND sw.deleted = 0")
    List<SensitiveWord> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("SELECT sw.* FROM sensitive_word sw WHERE sw.category = #{category} AND sw.tenant_id = #{tenantId} AND sw.deleted = 0")
    List<SensitiveWord> selectByCategory(@Param("category") String category, @Param("tenantId") Long tenantId);

    @Select("SELECT sw.word FROM sensitive_word sw WHERE sw.tenant_id = #{tenantId} AND sw.deleted = 0")
    List<String> selectWordsByTenantId(@Param("tenantId") Long tenantId);
}