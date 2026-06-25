-- FinRag4j Auth Service 数据库初始化脚本

-- 删除已有表（如果存在）
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

-- 创建用户表
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(500) NOT NULL,
    email VARCHAR(200),
    phone VARCHAR(20),
    avatar VARCHAR(500),
    status VARCHAR(20) DEFAULT 'normal',
    department_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 创建角色表
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sort INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'normal',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 创建权限表
CREATE TABLE sys_permission (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(20) DEFAULT 'menu',
    path VARCHAR(500),
    icon VARCHAR(100),
    sort INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'normal',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 创建用户角色关联表
CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, role_id)
);

-- 创建角色权限关联表
CREATE TABLE sys_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, permission_id)
);

-- 创建索引
CREATE INDEX idx_sys_user_username ON sys_user(username);
CREATE INDEX idx_sys_user_status ON sys_user(status);
CREATE INDEX idx_sys_role_code ON sys_role(code);
CREATE INDEX idx_sys_user_role_user ON sys_user_role(user_id);
CREATE INDEX idx_sys_user_role_role ON sys_user_role(role_id);

-- 插入默认数据

-- 默认角色
INSERT INTO sys_role (id, code, name, description, sort, status) VALUES
(1, 'admin', '超级管理员', '系统超级管理员，拥有所有权限', 0, 'normal');

-- 默认管理员用户 (密码: admin 经过BCrypt加密)
INSERT INTO sys_user (id, username, password, email, phone, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E.', 'admin@finrag4j.com', '13800138000', 'normal');

-- 关联用户和角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

-- 默认权限
INSERT INTO sys_permission (id, parent_id, name, code, type, path, icon, sort) VALUES
(1, 0, '系统管理', 'system', 'menu', '/system', 'system', 0),
(2, 1, '用户管理', 'sys:user:list', 'menu', '/system/users', 'user', 1),
(3, 1, '角色管理', 'sys:role:list', 'menu', '/system/roles', 'role', 2),
(4, 1, '权限管理', 'sys:permission:list', 'menu', '/system/permissions', 'permission', 3);

-- 超级管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4);