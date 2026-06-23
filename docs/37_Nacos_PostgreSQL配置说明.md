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
# Nacos 3.2.2 PostgreSQL 配置修复记录

## 问题背景

Nacos 3.2.2 在使用 PostgreSQL 数据库时，由于环境变量和数据库初始化脚本配置不当，导致启动失败。

## 问题分析

### 1. 环境变量冲突

Nacos 容器使用了 `MYSQL_SERVICE_*` 系列环境变量，这些变量是为 MySQL 设计的，会干扰 PostgreSQL 数据源配置，导致 Nacos 仍尝试用 MySQL 驱动连接数据库。

### 2. 数据库初始化问题

- PostgreSQL 初始化脚本未严格按照官方脚本执行
- `config_info_gray` 表缺少 `tenant_id` 列的 `NOT NULL DEFAULT ''` 约束
- 数据库名称和用户名不一致

### 3. 健康检查接口变化

Nacos 3.x 健康检查接口从 `/nacos/v1/console/health/readiness` 变更为 `/nacos/v3/admin/core/state/readiness`。

## 解决方案

### 1. 环境变量配置

**`.env` 文件配置：**

```bash
# PostgreSQL Configuration
POSTGRES_CONTAINER_NAME=finrag4j-postgres
POSTGRES_HOST=finrag4j-postgres  # 内部网络使用容器名称
POSTGRES_PORT=5432
POSTGRES_DB=nacos
POSTGRES_USER=nacos
POSTGRES_PASSWORD=nacos

# Nacos Configuration
NACOS_CONTAINER_NAME=finrag4j-nacos
NACOS_HOST=11.0.1.190
NACOS_PORT=8848
NACOS_PORT_GRPC=9848
NACOS_PORT_GRPC2=9849
NACOS_NAMESPACE=public

# Nacos 3.x 鉴权配置（必须）
NACOS_AUTH_ENABLE=true
NACOS_AUTH_IDENTITY_KEY=serverIdentity
NACOS_AUTH_IDENTITY_VALUE=mySecureValue123
NACOS_AUTH_TOKEN=VGhpc0lzTXlTZWNyZXRLZXlXaGljaFNob3VsZEJlMzJDaGFyYWN0ZXJzTGFyZ2U=
```

### 2. Nacos 配置文件

**`nacos-config/application.properties`：**

```properties
# ============ PostgreSQL 数据源配置 ============
spring.datasource.platform=postgresql
db.num=1
db.url.0=jdbc:postgresql://${POSTGRES_HOST:finrag4j-postgres}:${POSTGRES_PORT:5432}/${POSTGRES_DB:nacos}?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&currentSchema=public
db.user=${POSTGRES_USER:nacos}
db.password=${POSTGRES_PASSWORD:nacos}
db.pool.config.driverClassName=org.postgresql.Driver

# ============ Nacos 3.x 鉴权配置（必须）============
nacos.core.auth.enabled=${NACOS_AUTH_ENABLE:true}
nacos.core.auth.server.identity.key=${NACOS_AUTH_IDENTITY_KEY:serverIdentity}
nacos.core.auth.server.identity.value=${NACOS_AUTH_IDENTITY_VALUE:mySecureValue123}
nacos.core.auth.plugin.nacos.token.secret.key=${NACOS_AUTH_TOKEN:VGhpc0lzTXlTZWNyZXRLZXlXaGljaFNob3VsZEJlMzJDaGFyYWN0ZXJzTGFyZ2U=}
nacos.core.auth.caching.enabled=${NACOS_AUTH_CACHE_ENABLE:true}
```

### 3. 数据库初始化脚本

严格按照官方脚本执行，分为三个步骤：

| 脚本 | 说明 |
|------|------|
| `sql/01_create_nacos_user.sql` | 创建 nacos 用户和数据库 |
| `sql/02_pg_schema.sql` | 执行官方 `pg-schema.sql`（包含 `config_info_gray` 表） |
| `sql/03_pg_grant.sql` | 执行官方 `pg-grant-nacos-readwrite.sql` |

**关键表结构要求：**

所有包含 `tenant_id` 列的表必须定义为 `NOT NULL DEFAULT ''`：

```sql
"tenant_id" varchar(128) NOT NULL DEFAULT ''
```

### 4. Docker Compose 配置

**`docker-compose-base.yml`：**

```yaml
postgres:
  image: pgvector/pgvector:pg17
  environment:
    - POSTGRES_USER=${POSTGRES_USER:-nacos}
    - POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-nacos}
  volumes:
    - ./data/postgres:/var/lib/postgresql/data
    - ./sql/01_create_nacos_user.sql:/docker-entrypoint-initdb.d/01_create_nacos_user.sql
    - ./sql/02_pg_schema.sql:/docker-entrypoint-initdb.d/02_pg_schema.sql
    - ./sql/03_pg_grant.sql:/docker-entrypoint-initdb.d/03_pg_grant.sql

nacos:
  image: nacos/nacos-server:v3.2.2
  environment:
    - MODE=standalone
    - NACOS_SERVER_PORT=${NACOS_PORT:-8848}
    - JVM_XMS=256m
    - JVM_XMX=512m
    - JVM_XMN=128m
  volumes:
    - ./data/nacos/logs:/home/nacos/logs
    - ./data/nacos/data:/home/nacos/data
    - ./nacos-config/application.properties:/home/nacos/conf/application.properties:ro
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:${NACOS_PORT:-8848}/nacos/v3/admin/core/state/readiness"]
    interval: 15s
    timeout: 10s
    retries: 10
    start_period: 90s
```

### 5. 健康检查接口

| Nacos 版本 | 健康检查接口 |
|-----------|-------------|
| 2.x | `/nacos/v1/console/health/readiness` |
| 3.x | `/nacos/v3/admin/core/state/readiness` |

## 启动命令

```bash
cd ~/finrag4j/deploy

# 停止所有容器
docker-compose -f docker-compose-base.yml down

# 清理 PostgreSQL 数据（必须重新初始化）
rm -rf ./data/postgres/*

# 启动服务
docker-compose -f docker-compose-base.yml up -d

# 查看日志
docker logs -f finrag4j-nacos
```

## Nacos 登录信息

- 用户名: `nacos`
- 密码: `nacos`

## 验证清单

- [ ] PostgreSQL 启动成功
- [ ] Nacos 数据库和用户创建成功
- [ ] Nacos 表结构创建成功（包含 `config_info_gray` 表）
- [ ] Nacos 启动成功（无 schema 错误）
- [ ] 健康检查通过
- [ ] 登录 Nacos 控制台成功

## 更新的文件

| 文件 | 更新内容 |
|------|----------|
| `.env` | PostgreSQL 用户改为 nacos/nacos，内部网络使用容器名称 |
| `.env.devhome` | 同上 |
| `.env.devcompany` | 同上 |
| `.env.test` | 同上 |
| `.env.prod` | 同上 |
| `nacos-config/application.properties` | 使用环境变量配置，删除无用配置 |
| `docker-compose-base.yml` | 更新 PostgreSQL 环境变量、初始化脚本、健康检查 |
| `k8s/finrag4j-deployment.yaml` | 更新 PostgreSQL Secret、健康检查 |
| `k8s/nacos-config.yaml` | 更新 Nacos 配置 |
| `sql/01_create_nacos_user.sql` | 新建 - 创建 nacos 用户和数据库 |
| `sql/02_pg_schema.sql` | 新建 - 严格按照官方 pg-schema.sql |
| `sql/03_pg_grant.sql` | 新建 - 严格按照官方 pg-grant-nacos-readwrite.sql |

## 参考文件

- `d:\coding\github\finrag4j\deploy\nacos\pg-schema.sql` - Nacos 官方 PostgreSQL 表结构脚本
- `d:\coding\github\finrag4j\deploy\nacos\pg-grant-nacos-readwrite.sql` - Nacos 官方权限授予脚本
- `d:\coding\github\finrag4j\deploy\nacos\nacos_win11.txt` - Windows 11 验证通过的配置方法
