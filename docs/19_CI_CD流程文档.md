# FinRag4j CI/CD 流程文档

## 文档信息
- 版本：v1.0
- 更新日期：2026-06-25
- 适用版本：FinRag4j v1.0+

---

## 1. 流水线架构

```
┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│   代码提交   │──►│  构建阶段   │──►│  测试阶段   │──►│  发布阶段   │
│  (Git Push) │   │   (Build)   │   │   (Test)    │   │  (Deploy)   │
└─────────────┘   └──────┬──────┘   └──────┬──────┘   └──────┬──────┘
                         │                 │                 │
                    ┌────┴────┐       ┌────┴────┐       ┌────┴────┐
                    │代码编译  │       │单元测试  │       │镜像构建  │
                    │代码检查  │       │集成测试  │       │镜像推送  │
                    │         │       │安全扫描  │       │服务部署  │
                    └─────────┘       └─────────┘       └─────────┘
```

---

## 2. 分支策略

### 2.1 Git Flow 分支模型

| 分支 | 用途 | 保护规则 |
|------|------|---------|
| main | 生产环境代码 | 禁止直接推送，需PR合并 |
| release/* | 预发布版本 | 仅允许从develop合并 |
| develop | 开发集成 | 需PR审核 |
| feature/* | 功能开发 | 合并到develop后删除 |
| hotfix/* | 生产紧急修复 | 合并到main和develop |

### 2.2 提交规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型说明：**
- `feat`: 新功能
- `fix`: 修复
- `docs`: 文档
- `style`: 格式（不影响代码运行）
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具

**示例：**
```
feat(auth): 添加JWT刷新Token机制

- 实现 refreshToken 接口
- 添加 Token 过期自动刷新逻辑
- 更新单元测试

Closes #123
```

---

## 3. 构建阶段

### 3.1 Java 后端构建

```yaml
# .github/workflows/build.yml 示例
jobs:
  build-java:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Compile
        run: mvn clean compile -DskipTests
      
      - name: Code Quality Check
        run: |
          mvn spotbugs:check
          mvn checkstyle:check
      
      - name: Unit Tests
        run: mvn test
      
      - name: Integration Tests
        run: mvn verify -Pintegration-test
      
      - name: Coverage Report
        run: mvn jacoco:report
      
      - name: Package
        run: mvn package -DskipTests
```

### 3.2 Python 服务构建

```yaml
  build-python:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Python
        uses: actions/setup-python@v5
        with:
          python-version: '3.12'
      
      - name: Install uv
        run: pip install uv
      
      - name: Install Dependencies
        run: |
          cd backend/finrag4j-python
          uv sync
      
      - name: Lint
        run: |
          ruff check src/
          black --check src/
      
      - name: Unit Tests
        run: |
          pytest --cov=src --cov-report=xml
      
      - name: Coverage Check
        run: |
          pytest --cov-fail-under=75
```

### 3.3 前端构建

```yaml
  build-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install Dependencies
        run: |
          cd frontend
          npm ci
      
      - name: Lint
        run: npm run lint
      
      - name: Unit Tests
        run: npm run test:unit
      
      - name: Build
        run: npm run build
```

---

## 4. 镜像构建与推送

### 4.1 Dockerfile 规范

```dockerfile
# Java 服务 Dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4.2 镜像标签策略

| 标签 | 说明 | 示例 |
|------|------|------|
| latest | 最新稳定版 | finrag4j/gateway:latest |
| v{版本号} | 发布版本 | finrag4j/gateway:v1.2.0 |
| {Git短哈希} | 构建版本 | finrag4j/gateway:a3f7d2e |
| {分支名} | 开发版本 | finrag4j/gateway:develop |

### 4.3 镜像构建脚本

```bash
#!/bin/bash
# scripts/build-images.sh

VERSION=${1:-latest}
REGISTRY="your-registry.com/finrag4j"

for module in gateway auth document search agent; do
    echo "Building finrag4j-$module..."
    docker build \
        -t $REGISTRY/$module:$VERSION \
        -t $REGISTRY/$module:latest \
        -f backend/finrag4j-$module/Dockerfile \
        backend/finrag4j-$module
    
    docker push $REGISTRY/$module:$VERSION
    docker push $REGISTRY/$module:latest
done
```

---

## 5. 部署策略

### 5.1 环境划分

| 环境 | 部署触发 | 用途 | 数据 |
|------|---------|------|------|
| Dev | 每次PR合并 | 功能验证 | 模拟数据 |
| Test | 每日定时 | 集成测试 | 测试数据 |
| Staging | 发布前手动 | UAT验收 | 脱敏生产数据 |
| Prod | 审批后手动 | 生产环境 | 真实数据 |

### 5.2 滚动发布

```yaml
# docker-compose.prod.yml 部署配置
services:
  gateway:
    image: finrag4j/gateway:${VERSION}
    deploy:
      replicas: 2
      update_config:
        parallelism: 1        # 每次更新1个实例
        delay: 30s            # 间隔30秒
        failure_action: rollback
      rollback_config:
        parallelism: 1
        delay: 10s
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8085/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
```

### 5.3 数据库迁移

```yaml
# 迁移Job配置
services:
  db-migrate:
    image: finrag4j/migrate:${VERSION}
    command: ["flyway", "migrate"]
    environment:
      FLYWAY_URL: jdbc:postgresql://postgres:5432/finrag4j
      FLYWAY_USER: ${DB_USER}
      FLYWAY_PASSWORD: ${DB_PASSWORD}
    depends_on:
      postgres:
        condition: service_healthy
```

---

## 6. 安全扫描

### 6.1 依赖漏洞扫描

```yaml
      - name: Dependency Check
        run: mvn org.owasp:dependency-check-maven:check

      - name: Python Safety Check
        run: |
          pip install safety
          safety check -r requirements.txt
```

### 6.2 镜像安全扫描

```yaml
      - name: Trivy Scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: finrag4j/gateway:${{ github.sha }}
          format: 'sarif'
          output: 'trivy-results.sarif'
```

---

## 7. 发布流程

### 7.1 版本发布 Checklist

1. [ ] 所有测试通过
2. [ ] 代码审查完成
3. [ ] 安全扫描无高危漏洞
4. [ ] 变更日志更新
5. [ ] 数据库迁移脚本验证
6. [ ] 生产配置确认
7. [ ] 回滚方案准备

### 7.2 版本号规范

遵循 [语义化版本](https://semver.org/lang/zh-CN/)：

```
主版本号.次版本号.修订号
 1.   2.    3
```

- **主版本号**：不兼容的 API 修改
- **次版本号**：向下兼容的功能新增
- **修订号**：向下兼容的问题修复

### 7.3 发布审批

```
开发者提交发布申请
      │
      ▼
技术负责人审核代码
      │
      ▼
测试负责人确认测试通过
      │
      ▼
运维负责人确认部署方案
      │
      ▼
项目经理审批发布
      │
      ▼
执行发布（双人复核）
```

---

## 8. 回滚策略

### 8.1 快速回滚

```bash
#!/bin/bash
# scripts/rollback.sh

SERVICE=$1
LAST_VERSION=$(docker images --format "{{.Tag}}" $REGISTRY/$SERVICE | grep -v latest | head -2 | tail -1)

echo "Rolling back $SERVICE to $LAST_VERSION..."
docker service update \
    --image $REGISTRY/$SERVICE:$LAST_VERSION \
    finrag4j_$SERVICE

echo "Rollback completed."
```

### 8.2 数据库回滚

```bash
# 回滚到上一个版本
flyway undo

# 或指定目标版本
flyway migrate -target=1.1.0
```

---

## 9. 监控与告警

### 9.1 流水线状态通知

```yaml
      - name: Notify Slack
        if: always()
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          fields: repo,message,commit,author,action,eventName,ref,workflow
```

### 9.2 部署状态追踪

| 阶段 | 状态 | 通知渠道 |
|------|------|---------|
| 构建开始 | 进行中 | Slack |
| 构建失败 | 失败 | Slack + 邮件 |
| 测试通过 | 成功 | Slack |
| 部署开始 | 进行中 | Slack |
| 部署完成 | 成功 | Slack |
| 部署失败 | 失败 | Slack + 邮件 + 短信 |
