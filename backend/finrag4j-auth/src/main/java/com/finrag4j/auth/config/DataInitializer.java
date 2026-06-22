package com.finrag4j.auth.config;

import com.finrag4j.auth.entity.SysUser;
import com.finrag4j.auth.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据初始化器
 * 确保默认管理员用户存在
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 检查管理员用户是否存在
        SysUser adminUser = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, "admin")
        );

        if (adminUser == null) {
            log.info("正在创建默认管理员用户...");
            
            SysUser user = new SysUser();
            user.setUsername("admin");
            // 密码: admin 经过BCrypt加密
            user.setPassword(passwordEncoder.encode("admin"));
            user.setEmail("admin@finrag4j.com");
            user.setPhone("13800138000");
            user.setStatus("normal");
            user.setDepartmentId(1L);
            
            userMapper.insert(user);
            log.info("默认管理员用户创建成功: admin / admin");
        } else {
            log.info("管理员用户已存在，跳过创建");
        }
    }
}
