package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.RecycleBin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 回收站Mapper接口
 */
@Mapper
public interface RecycleBinMapper extends BaseMapper<RecycleBin> {

    @Select("SELECT rb.* FROM recycle_bin rb WHERE rb.tenant_id = #{tenantId} AND rb.deleted = 0 ORDER BY rb.delete_time DESC")
    List<RecycleBin> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("SELECT rb.* FROM recycle_bin rb WHERE rb.resource_type = #{resourceType} AND rb.resource_id = #{resourceId} AND rb.tenant_id = #{tenantId} AND rb.deleted = 0")
    RecycleBin selectByResource(@Param("resourceType") String resourceType, @Param("resourceId") Long resourceId, @Param("tenantId") Long tenantId);

    @Delete("DELETE FROM recycle_bin WHERE expire_time < NOW() AND deleted = 0")
    int deleteExpired();
}