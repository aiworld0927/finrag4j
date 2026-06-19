package com.finrag4j.auth.service;

import com.finrag4j.auth.entity.SysPermission;
import com.finrag4j.auth.mapper.SysPermissionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务
 */
@Service
@RequiredArgsConstructor
public class PermissionService extends ServiceImpl<SysPermissionMapper, SysPermission> {

    private final SysPermissionMapper permissionMapper;

    /**
     * 获取权限树
     */
    public List<SysPermission> getPermissionTree() {
        List<SysPermission> all = permissionMapper.selectList(null);
        return buildTree(all);
    }

    /**
     * 构建权限树
     */
    private List<SysPermission> buildTree(List<SysPermission> permissions) {
        Map<Long, List<SysPermission>> grouped = permissions.stream()
                .filter(p -> p.getParentId() != null && p.getParentId() != 0)
                .collect(Collectors.groupingBy(SysPermission::getParentId));

        List<SysPermission> result = new ArrayList<>();
        for (SysPermission root : permissions) {
            if (root.getParentId() == null || root.getParentId() == 0) {
                result.add(root);
            }
        }

        return result;
    }
}
