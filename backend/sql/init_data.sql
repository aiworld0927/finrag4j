-- FinRag4j 数据库初始化脚本 (PostgreSQL)
-- 默认管理员用户: admin / admin

-- 创建默认租户
INSERT INTO sys_tenant (id, name, code, status, create_time, update_time, deleted)
VALUES (1, '默认租户', 'default', 'normal', NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- 创建默认部门
INSERT INTO sys_department (id, name, parent_id, status, create_time, update_time, deleted)
VALUES (1, '总经办', 0, 'normal', NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- 创建默认角色
INSERT INTO sys_role (id, name, code, description, status, create_time, update_time, deleted)
VALUES (1, '超级管理员', 'admin', '系统超级管理员，拥有所有权限', 'normal', NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- 创建默认管理员用户 (密码: admin 经过BCrypt加密)
-- 加密后的密码为: $2a$10$xxx...
INSERT INTO sys_user (id, username, password, email, phone, status, department_id, create_time, update_time, deleted)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.', 'admin@finrag4j.com', '13800138000', 'normal', 1, NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- 关联用户和角色
INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1)
ON CONFLICT DO NOTHING;
