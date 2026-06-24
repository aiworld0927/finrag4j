-- 1. 创建 Nacos 专用用户（请将 'your_strong_password' 替换为强密码）
CREATE USER nacos WITH PASSWORD 'nacos';

-- 2. 授予该用户创建数据库的权限
ALTER USER nacos WITH CREATEDB;

-- 3. 创建数据库 nacos，并设置其所有者为 nacos 用户
CREATE DATABASE nacos OWNER nacos;

-- 4. 授予 nacos 用户对 nacos 数据库的全部权限
GRANT ALL PRIVILEGES ON DATABASE nacos TO nacos;