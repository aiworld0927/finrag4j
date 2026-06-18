package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode} AND deleted = 0")
    SysPermission selectByCode(@Param("permissionCode") String permissionCode);

    @Select("SELECT * FROM sys_permission WHERE module = #{module} AND deleted = 0")
    List<SysPermission> selectByModule(@Param("module") String module);

    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0")
    List<SysPermission> selectByParentId(@Param("parentId") Long parentId);
}