# Nacos 版本选择说明

## 文档版本
- 版本：v1.3
- 创建日期：2026-06-19
- 更新说明：使用 Nacos v3.2.2（与 PostgreSQL 17 验证通过）

---

## 1. 当前使用版本

**推荐版本：Nacos v3.2.2**

| 配置项 | 值 |
|--------|-----|
| 镜像版本 | `nacos/nacos-server:v3.2.2` |
| 数据库 | PostgreSQL 17 |
| 模式 | Standalone |
| 验证状态 | ✅ 已验证 |

---

## 2. 版本选择原因

### 2.1 为什么选择 v3.2.2

| 原因 | 说明 |
|------|------|
| **PostgreSQL 17 兼容** | 官方验证通过，稳定可靠 |
| **功能增强** | 支持 NEP（Nacos Extension Protocol） |
| **安全增强** | 强制鉴权配置，安全性更高 |
| **性能优化** | 相比 v2.x 有更好的性能表现 |

### 2.2 鉴权配置说明

Nacos 3.x 强制要求配置以下鉴权参数：

| 参数 | 值 | 说明 |
|------|-----|------|
| `NACOS_AUTH_ENABLE` | `true` | 启用鉴权 |
| `NACOS_AUTH_IDENTITY_KEY` | `serverIdentity` | 服务器身份密钥 |
| `NACOS_AUTH_IDENTITY_VALUE` | `mySecureValue123` | 服务器身份值 |
| `NACOS_AUTH_TOKEN` | Base64 String | 令牌密钥（Base64编码） |

---

## 3. 版本对比

| 维度 | v3.2.2 | v2.x |
|------|--------|------|
| PostgreSQL 17 支持 | ✅ 验证通过 | ✅ 支持 |
| 安全配置 | 强制 | 可选 |
| 启动稳定性 | 高 | 高 |
| 资源占用 | 较高 | 较低 |
| Java 版本 | Java 17+ | Java 8+ |

---

## 4. Docker Compose 配置说明

### 4.1 核心配置

```yaml
nacos:
  image: nacos/nacos-server:v3.2.2
  environment:
    - MODE=standalone
    - SPRING_DATASOURCE_PLATFORM=postgresql
    - MYSQL_SERVICE_HOST=postgres
    - MYSQL_SERVICE_PORT=5432
    - MYSQL_SERVICE_DB_NAME=finrag4j_nacos
    - MYSQL_SERVICE_USER=postgres
    - MYSQL_SERVICE_PASSWORD=postgres
    # Nacos 3.x 鉴权配置（必须）
    - NACOS_AUTH_ENABLE=true
    - NACOS_AUTH_IDENTITY_KEY=serverIdentity
    - NACOS_AUTH_IDENTITY_VALUE=mySecureValue123
    - NACOS_AUTH_TOKEN=VGhpc0lzTXlTZWNyZXRLZXlXaGljaFNob3VsZEJlMzJDaGFyYWN0ZXJzTGFyZ2U=
```

### 4.2 .env 文件配置

```bash
# Nacos 3.x 鉴权配置（必须）
NACOS_AUTH_ENABLE=true
NACOS_AUTH_IDENTITY_KEY=serverIdentity
NACOS_AUTH_IDENTITY_VALUE=mySecureValue123
NACOS_AUTH_TOKEN=VGhpc0lzTXlTZWNyZXRLZXlXaGljaFNob3VsZEJlMzJDaGFyYWN0ZXJzTGFyZ2U=
```

---

## 5. 结论

**推荐使用 Nacos v3.2.2**，原因：
1. 与 PostgreSQL 17 官方验证通过
2. 功能更强大，支持 NEP 协议
3. 安全性更高，强制鉴权机制
4. 性能相比 v2.x 有所优化

**配置要求**：必须正确配置鉴权参数才能启动成功。
