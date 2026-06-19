package com.finrag4j.auth.service;

import com.finrag4j.auth.entity.SysRole;
import com.finrag4j.auth.entity.SysRolePermission;
import com.finrag4j.auth.mapper.SysRoleMapper;
import com.finrag4j.auth.mapper.SysRolePermissionMapper;
import com.finrag4j.common.BusinessException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务
 */
@Service
@RequiredArgsConstructor
public class RoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public void create(SysRole role) {
        // 检查角色编码唯一性
        SysRole exist = roleMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getCode, role.getCode())
        );
        if (exist != null) {
            throw new BusinessException(400, "角色编码已存在");
        }
        roleMapper.insert(role);
    }

    public void update(SysRole role) {
        roleMapper.updateById(role);
    }

    public void delete(Long id) {
        roleMapper.deleteById(id);
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限
        rolePermissionMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, roleId)
        );

        // 添加新权限
        for (Long permissionId : permissionIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rolePermissionMapper.insert(rp);
        }
    }
}
