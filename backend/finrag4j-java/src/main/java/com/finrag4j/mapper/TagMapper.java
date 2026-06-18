package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签Mapper接口
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    @Select("SELECT t.* FROM tag t WHERE t.tenant_id = #{tenantId} AND t.deleted = 0")
    List<Tag> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("SELECT t.* FROM tag t WHERE t.tag_code = #{tagCode} AND t.deleted = 0")
    Tag selectByCode(@Param("tagCode") String tagCode);
}