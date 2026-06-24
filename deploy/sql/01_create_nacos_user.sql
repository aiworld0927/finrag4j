-- ============================================
-- FinRag4j Nacos 数据库初始化脚本 - 步骤1
-- 创建 nacos 用户和数据库
-- ============================================

-- 创建 Nacos 用户
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'nacos') THEN
        CREATE USER nacos WITH PASSWORD 'nacos';
        RAISE NOTICE 'User nacos created successfully';
    ELSE
        RAISE NOTICE 'User nacos already exists, skipping...';
    END IF;
END
$$;

-- 切换到 postgres 数据库并创建 nacos 数据库
\c postgres
DROP DATABASE IF EXISTS nacos;
CREATE DATABASE nacos;

-- 授予连接权限
GRANT CONNECT ON DATABASE nacos TO nacos;

-- ============================================
-- 完成步骤1
-- ============================================
