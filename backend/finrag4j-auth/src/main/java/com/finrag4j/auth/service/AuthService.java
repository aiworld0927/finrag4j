package com.finrag4j.auth.service;

import com.finrag4j.auth.dto.RegisterRequest;
import com.finrag4j.auth.entity.SysUser;
import com.finrag4j.auth.mapper.SysUserMapper;
import com.finrag4j.common.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getStatus, "normal")
        );

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        return jwtService.generateTokens(user);
    }

    /**
     * 用户注册
     */
    public void register(RegisterRequest request) {
        // 检查用户名是否存在
        SysUser existUser = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
        );

        if (existUser != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus("normal");

        userMapper.insert(user);
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        // 可以将token加入黑名单
        jwtService.blacklistToken(token);
    }

    /**
     * 刷新Token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        return jwtService.refreshTokens(refreshToken);
    }

    /**
     * 获取当前用户信息
     */
    public SysUser getCurrentUser(String token) {
        Long userId = jwtService.getUserIdFromToken(token);
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }
}
