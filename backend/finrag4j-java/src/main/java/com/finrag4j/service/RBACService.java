package com.finrag4j.service;

import com.finrag4j.common.exception.BusinessException;
import com.finrag4j.entity.*;
import com.finrag4j.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RBAC权限服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RBACService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final DepartmentMapper departmentMapper;
    private final TenantMapper tenantMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==================== 租户管理 ====================

    /**
     * 创建租户（商业版功能）
     */
    @Transactional
    public Tenant createTenant(Tenant tenant) {
        // 检查租户编码是否重复
        if (tenantMapper.selectByCode(tenant.getTenantCode()) != null) {
            throw new BusinessException("租户编码已存在");
        }
        
        tenantMapper.insert(tenant);
        log.info("创建租户: {}", tenant.getTenantName());
        return tenant;
    }

    /**
     * 获取租户列表（商业版功能）
     */
    public List<Tenant> getTenants() {
        return tenantMapper.selectList(null).stream()
                .filter(t -> t.getDeleted() == 0)
                .collect(Collectors.toList());
    }

    // ==================== 用户管理 ====================

    /**
     * 创建用户
     */
    @Transactional
    public SysUser createUser(SysUser user) {
        // 检查用户名是否重复
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        log.info("创建用户: {}", user.getUsername());
        return user;
    }

    /**
     * 更新用户
     */
    @Transactional
    public SysUser updateUser(Long id, SysUser user) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setId(id);
        userMapper.updateById(user);
        return user;
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setDeleted(1);
        userMapper.updateById(user);
        log.info("删除用户: {}", user.getUsername());
    }

    /**
     * 根据用户名获取用户
     */
    public SysUser getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 获取用户列表
     */
    public List<SysUser> getUsers(Long tenantId) {
        return userMapper.selectByTenantId(tenantId);
    }

    /**
     * 用户登录验证
     */
    public SysUser authenticate(String username, String password) {
        SysUser user = getUserByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return user;
    }

    // ==================== 角色管理 ====================

    /**
     * 创建角色
     */
    @Transactional
    public SysRole createRole(SysRole role) {
        if (roleMapper.selectByCode(role.getRoleCode()) != null) {
            throw new BusinessException("角色编码已存在");
        }
        roleMapper.insert(role);
        log.info("创建角色: {}", role.getRoleName());
        return role;
    }

    /**
     * 更新角色
     */
    @Transactional
    public SysRole updateRole(Long id, SysRole role) {
        SysRole existing = roleMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        if (existing.getIsSystem() == 1) {
            throw new BusinessException("系统角色不能修改");
        }
        role.setId(id);
        roleMapper.updateById(role);
        return role;
    }

    /**
     * 删除角色
     */
    @Transactional
    public void deleteRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if (role.getIsSystem() == 1) {
            throw new BusinessException("系统角色不能删除");
        }
        role.setDeleted(1);
        roleMapper.updateById(role);
        log.info("删除角色: {}", role.getRoleName());
    }

    /**
     * 获取角色列表
     */
    public List<SysRole> getRoles(Long tenantId) {
        return roleMapper.selectByTenantId(tenantId);
    }

    // ==================== 权限管理 ====================

    /**
     * 获取所有权限
     */
    public List<SysPermission> getAllPermissions() {
        return permissionMapper.selectList(null).stream()
                .filter(p -> p.getDeleted() == 0)
                .collect(Collectors.toList());
    }

    /**
     * 根据模块获取权限
     */
    public List<SysPermission> getPermissionsByModule(String module) {
        return permissionMapper.selectByModule(module);
    }

    // ==================== 用户角色关联 ====================

    /**
     * 给用户分配角色
     */
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds, Long tenantId) {
        // 先删除原有角色
        userRoleMapper.deleteByUserId(userId);
        
        // 添加新角色
        for (Long roleId : roleIds) {
            UserRole userRole = UserRole.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .tenantId(tenantId)
                    .build();
            userRoleMapper.insert(userRole);
        }
        log.info("用户 {} 分配角色: {}", userId, roleIds);
    }

    /**
     * 获取用户角色
     */
    public List<SysRole> getUserRoles(Long userId, Long tenantId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId, tenantId);
        return roleIds.stream()
                .map(roleMapper::selectById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ==================== 角色权限关联 ====================

    /**
     * 给角色分配权限
     */
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds, Long tenantId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role != null && role.getIsSystem() == 1) {
            throw new BusinessException("系统角色权限不能修改");
        }
        
        // 先删除原有权限
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 添加新权限
        for (Long permissionId : permissionIds) {
            RolePermission rp = RolePermission.builder()
                    .roleId(roleId)
                    .permissionId(permissionId)
                    .tenantId(tenantId)
                    .build();
            rolePermissionMapper.insert(rp);
        }
        log.info("角色 {} 分配权限: {}", roleId, permissionIds);
    }

    /**
     * 获取角色权限
     */
    public List<SysPermission> getRolePermissions(Long roleId) {
        List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
        return permissionIds.stream()
                .map(permissionMapper::selectById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ==================== 权限验证 ====================

    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(Long userId, String permissionCode, Long tenantId) {
        List<SysRole> roles = getUserRoles(userId, tenantId);
        for (SysRole role : roles) {
            List<SysPermission> permissions = getRolePermissions(role.getId());
            if (permissions.stream().anyMatch(p -> permissionCode.equals(p.getPermissionCode()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取用户所有权限
     */
    public Set<String> getUserPermissions(Long userId, Long tenantId) {
        Set<String> permissions = new HashSet<>();
        List<SysRole> roles = getUserRoles(userId, tenantId);
        for (SysRole role : roles) {
            List<SysPermission> rolePermissions = getRolePermissions(role.getId());
            rolePermissions.forEach(p -> permissions.add(p.getPermissionCode()));
        }
        return permissions;
    }

    // ==================== 部门管理 ====================

    /**
     * 创建部门
     */
    @Transactional
    public Department createDepartment(Department department) {
        if (departmentMapper.selectByCode(department.getDeptCode()) != null) {
            throw new BusinessException("部门编码已存在");
        }
        departmentMapper.insert(department);
        log.info("创建部门: {}", department.getDeptName());
        return department;
    }

    /**
     * 获取部门列表
     */
    public List<Department> getDepartments(Long tenantId) {
        return departmentMapper.selectByTenantId(tenantId);
    }
}