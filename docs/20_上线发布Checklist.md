# FinRag4j 上线发布 Checklist

## 文档信息
- 版本：v1.0
- 更新日期：2026-06-25
- 适用版本：FinRag4j v1.0+

---

## 1. 发布前准备

### 1.1 代码与构建

- [ ] **版本号确认**
  - [ ] pom.xml 版本号已更新
  - [ ] package.json 版本号已更新
  - [ ] Dockerfile 标签已指定版本
  - [ ] Git Tag 已打（如 v1.2.0）

- [ ] **代码审查**
  - [ ] 所有变更已通过 Code Review
  - [ ] 无未解决的评论
  - [ ] 分支保护规则检查通过

- [ ] **构建验证**
  - [ ] CI Pipeline 全部通过
  - [ ] 单元测试通过率 100%
  - [ ] 集成测试通过率 100%
  - [ ] 代码覆盖率达标（>75%）
  - [ ] 安全扫描无高危漏洞

### 1.2 数据库

- [ ] **迁移脚本**
  - [ ] Flyway/Liquibase 脚本已编写
  - [ ] 脚本在测试环境执行成功
  - [ ] 回滚脚本已准备
  - [ ] 大数据量迁移已评估性能影响

- [ ] **数据备份**
  - [ ] 生产数据库已完整备份
  - [ ] 备份文件已验证可恢复
  - [ ] 备份保留策略已确认

### 1.3 配置与环境

- [ ] **环境变量**
  - [ ] `.env.production` 已更新
  - [ ] 所有配置项已核对
  - [ ] 敏感配置已加密或放入 Vault
  - [ ] Nacos 生产配置已发布

- [ ] **中间件状态**
  - [ ] PostgreSQL 运行正常
  - [ ] Redis 运行正常
  - [ ] Nacos 集群运行正常
  - [ ] RocketMQ 运行正常
  - [ ] MinIO 运行正常

---

## 2. 发布执行

### 2.1 发布窗口

- [ ] **时间确认**
  - [ ] 发布窗口已申请并获批
  - [ ] 业务低峰期（建议凌晨 02:00-06:00）
  - [ ] 发布时长评估（预计 __ 分钟）
  - [ ] 回退时间预留（预计 __ 分钟）

- [ ] **人员确认**
  - [ ] 发布负责人：________
  - [ ] 技术值班：________
  - [ ] 业务联系人：________
  - [ ] 紧急联系方式已确认

### 2.2 发布步骤

#### 步骤1：预发布检查（5分钟）

- [ ] 当前生产服务状态正常
- [ ] 监控告警已临时静默非关键告警
- [ ] 发布脚本已准备就绪

#### 步骤2：数据库迁移（10分钟）

```bash
# 执行数据库迁移
flyway migrate -configFiles=flyway.prod.conf

# 验证迁移结果
flyway info -configFiles=flyway.prod.conf
```

- [ ] 迁移脚本执行成功
- [ ] 数据一致性检查通过

#### 步骤3：服务发布（30分钟）

**发布顺序（严格按依赖顺序）：**

| 顺序 | 服务 | 操作 | 验证 |
|------|------|------|------|
| 1 | finrag4j-auth | 滚动更新 | 登录接口正常 |
| 2 | finrag4j-document | 滚动更新 | 文档接口正常 |
| 3 | finrag4j-search | 滚动更新 | 检索接口正常 |
| 4 | finrag4j-agent | 滚动更新 | Agent接口正常 |
| 5 | finrag4j-gateway | 滚动更新 | 路由转发正常 |
| 6 | frontend | 静态资源更新 | 页面访问正常 |
| 7 | python-service | 滚动更新 | 解析服务正常 |

```bash
# 滚动发布示例
for service in auth document search agent gateway; do
    echo "Deploying finrag4j-$service..."
    docker service update \
        --image finrag4j/$service:v1.2.0 \
        --update-parallelism 1 \
        --update-delay 30s \
        --update-failure-action rollback \
        finrag4j_$service
    
    # 等待健康检查通过
    sleep 60
done
```

- [ ] 每个服务健康检查通过
- [ ] 服务间调用链路正常

#### 步骤4：功能验证（15分钟）

**核心功能冒烟测试：**

- [ ] **认证模块**
  - [ ] 用户登录成功
  - [ ] Token 刷新正常
  - [ ] 权限校验正常

- [ ] **文档模块**
  - [ ] 文档上传成功
  - [ ] 文档解析成功
  - [ ] 文档检索成功

- [ ] **RAG问答**
  - [ ] 新建对话成功
  - [ ] 问答返回正常
  - [ ] 来源溯源正常

- [ ] **系统管理**
  - [ ] 用户管理正常
  - [ ] 知识库管理正常

---

## 3. 发布后观察

### 3.1 监控观察（发布后1小时）

- [ ] **系统指标**
  - [ ] CPU/内存/磁盘正常
  - [ ] 各服务Pod/容器运行正常
  - [ ] 网络流量无异常

- [ ] **应用指标**
  - [ ] 错误率 < 0.1%
  - [ ] P99延迟正常
  - [ ] 吞吐量平稳

- [ ] **业务指标**
  - [ ] 用户登录成功
  - [ ] 核心业务流程正常
  - [ ] 无用户投诉

### 3.2 日志检查

- [ ] 无 ERROR 级别日志
- [ ] WARN 日志已审查（确认非新引入）
- [ ] 关键业务日志正常输出

---

## 4. 回滚预案

### 4.1 回滚触发条件

满足以下任一条件立即回滚：
- [ ] 核心功能不可用
- [ ] 错误率 > 5%
- [ ] P99延迟 > 10秒（持续5分钟）
- [ ] 数据丢失或损坏
- [ ] 安全漏洞暴露

### 4.2 回滚步骤

```bash
#!/bin/bash
# rollback.sh

VERSION=$1  # 回滚目标版本

echo "===== 开始回滚到 $VERSION ====="

# 1. 停止当前发布
for service in gateway agent search document auth; do
    docker service update \
        --image finrag4j/$service:$VERSION \
        finrag4j_$service
done

# 2. 数据库回滚（如需要）
# flyway undo
# 或执行回滚脚本

# 3. 验证服务状态
echo "===== 验证服务状态 ====="
docker service ls

# 4. 功能验证
curl -f http://localhost:8085/actuator/health

echo "===== 回滚完成 ====="
```

---

## 5. 发布后总结

### 5.1 发布记录

| 项目 | 内容 |
|------|------|
| 发布版本 | v____ |
| 发布时间 | ____年__月__日 __:__ |
| 发布负责人 | ________ |
| 发布时长 | ____分钟 |
| 是否回滚 | 是 / 否 |
| 回滚原因 | ________ |

### 5.2 问题记录

| 序号 | 问题描述 | 影响 | 解决方案 | 状态 |
|------|---------|------|---------|------|
| 1 | | | | |
| 2 | | | | |

### 5.3 改进建议

- ________________________________
- ________________________________

---

## 附录：常用命令

```bash
# 查看服务状态
docker service ls
docker service ps finrag4j_gateway

# 查看日志
docker service logs finrag4j_auth -f --tail 100

# 查看健康检查
curl http://localhost:8085/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8083/actuator/health

# 数据库连接检查
docker exec finrag4j-postgres psql -U nacos -c "SELECT version();"

# Redis 检查
docker exec finrag4j-redis redis-cli ping

# Nacos 检查
curl http://localhost:8848/nacos/v1/ns/operator/metrics
```
