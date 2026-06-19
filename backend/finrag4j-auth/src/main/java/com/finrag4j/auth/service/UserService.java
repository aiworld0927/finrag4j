package com.finrag4j.auth.service;

import com.finrag4j.auth.entity.SysUser;
import com.finrag4j.auth.entity.SysUserRole;
import com.finrag4j.auth.mapper.SysUserMapper;
import com.finrag4j.auth.mapper.SysUserRoleMapper;
import com.finrag4j.common.BusinessException;
import com.finrag4j.common.PageRequest;
import com.finrag4j.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<SysUser> pageQuery(PageRequest request, String username, String status) {
        request.validate();
        Page<SysUser> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like(SysUser::getUsername, username);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(SysUser::getStatus, status);
        }

        Page<SysUser> result = userMapper.selectPage(page, queryWrapper);
        return PageResult.of(result.getTotal(), result.getRecords(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    public void create(SysUser user) {
        // 检查用户名唯一性
        SysUser exist = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername())
        );
        if (exist != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    public void update(SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    public void updateStatus(Long id, String status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        // 添加新角色
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }
}
