package com.finrag4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finrag4j.entity.UserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户角色关联Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Select("SELECT role_id FROM user_role WHERE user_id = #{userId} AND tenant_id = #{tenantId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    @Delete("DELETE FROM user_role WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}