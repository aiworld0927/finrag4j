-- ============================================
-- FinRag4j Nacos 数据库初始化脚本 - 步骤3
-- 严格按照官方 pg-grant-nacos-readwrite.sql 执行
-- ============================================

-- 切换到 nacos 数据库
\c nacos

-- 授予权限
GRANT USAGE ON SCHEMA public TO nacos;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO nacos;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO nacos;

-- 设置默认权限
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO nacos;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO nacos;

-- ============================================
-- 完成步骤3
-- ============================================
