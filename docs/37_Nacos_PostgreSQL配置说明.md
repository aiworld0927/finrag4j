# Nacos + PostgreSQL 配置说明

## 文档版本
- 版本：v1.1
- 创建日期：2026-06-19
- 更新日期：2026-06-19

---

## 1. 问题说明

### 1.1 原始问题
Nacos 启动失败，原因是数据库连接错误。

### 1.2 原因分析
原配置文件使用 `SPRING_DATASOURCE_PLATFORM=mysql`，但实际中间件使用的是 PostgreSQL 数据库，导致连接失败。

### 1.3 初始化脚本不执行问题
PostgreSQL 的 `/docker-entrypoint-initdb.d/` 脚本**只在首次启动且数据目录为空时执行**。如果之前已启动过 PostgreSQL，数据目录已有数据，脚本不会执行。

---

## 2. 解决方案

### 2.1 添加初始化服务

在 `docker-compose-base.yml` 中添加 `postgres-init` 服务：

```yaml
# PostgreSQL 初始化服务（执行 Nacos 数据库脚本）
postgres-init:
  image: postgres:17
  container_name: finrag4j-postgres-init
  env_file: .env
  volumes:
    - ./sql:/sql
  networks:
    - finrag4j-network
  depends_on:
    postgres:
      condition: service_healthy
  entrypoint: |
    sh -c "
      # 检查数据库是否存在
      DB_EXISTS=$(PGPASSWORD=$${POSTGRES_PASSWORD} psql -h postgres -U $${POSTGRES_USER:-postgres} -tAc \"SELECT 1 FROM pg_database WHERE datname = 'finrag4j_nacos'\");

      if [ '$$DB_EXISTS' != '1' ]; then
        # 创建数据库并执行脚本
        PGPASSWORD=$${POSTGRES_PASSWORD} psql -h postgres -U $${POSTGRES_USER:-postgres} -c 'CREATE DATABASE finrag4j_nacos;';
        PGPASSWORD=$${POSTGRES_PASSWORD} psql -h postgres -U $${POSTGRES_USER:-postgres} -d finrag4j_nacos -f /sql/nacos_postgresql.sql;
        echo 'Nacos database initialized successfully!';
      else
        echo 'Database finrag4j_nacos already exists, skipping initialization.';
      fi;
    "
  restart: "no"
```

### 2.2 工作原理

```
1. PostgreSQL 启动并通过健康检查
2. postgres-init 服务启动
3. 检查 finrag4j_nacos 数据库是否存在
4. 如果不存在，创建数据库并执行 SQL 脚本
5. 如果已存在，跳过初始化
6. postgres-init 容器退出（restart: "no"）
```

### 2.3 文件结构

```
deploy/
├── docker-compose-base.yml
├── .env
└── sql/
    └── nacos_postgresql.sql    # Nacos 数据库初始化脚本
```

---

## 4. 启动顺序

为避免启动顺序问题，已配置 Nacos 依赖 PostgreSQL：

```yaml
nacos:
  depends_on:
    postgres:
      condition: service_healthy
```

**启动顺序**：
1. PostgreSQL 启动并通过健康检查
2. Nacos 启动并连接数据库

---

## 5. Nacos 默认信息

| 项目 | 值 |
|------|-----|
| 控制台地址 | http://11.0.1.190:8848/nacos |
| 用户名 | nacos |
| 密码 | nacos |

---

## 6. 常见问题

### 6.1 数据库已存在

如果数据库已存在但表未创建，可以手动执行 SQL：

```bash
docker exec -it finrag4j-postgres psql -U postgres -d finrag4j_nacos -c "$(cat /docker-entrypoint-initdb.d/01-nacos.sql)"
```

### 3.3 初始化脚本位置

```
deploy/sql/
├── nacos_postgresql.sql    # Nacos 表结构 SQL
└── init_nacos_db.py       # Python 初始化脚本
```

### 6.3 重新初始化

如果需要完全重新初始化，需要：

1. 删除 PostgreSQL 数据目录：
```bash
rm -rf ./data/postgres
```

2. 重新启动：
```bash
docker-compose -f docker-compose-base.yml down
docker-compose -f docker-compose-base.yml up -d
```

---

## 7. 资源优化

Nacos 已配置为最小资源模式：

| 配置项 | 值 |
|--------|-----|
| JVM 堆内存 | 256MB - 512MB |
| 容器限制 | 768MB |
| 启动等待 | 60秒 |

---

**文档版本**: v1.0.0
**更新日期**: 2026-06-19
