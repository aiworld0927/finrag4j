# RocketMQ 运行失败分析与解决方案

## 文档版本
- 版本：v1.0
- 创建日期：2026-06-19

---

## 1. 常见失败原因分析

### 1.1 内存不足

**症状：**
```bash
# 容器启动后立即退出
docker ps -a | grep rocketmq
# 状态显示 Exited (1)

# 日志显示
Java heap space
OutOfMemoryError
```

**原因分析：**

RocketMQ 默认配置需要较大内存：
- NameServer: 默认 4GB 堆内存
- Broker: 默认 8GB 堆内存

**解决方案：**

当前配置已优化为小内存模式：

```yaml
# NameServer
environment:
  - JAVA_OPT=-Xms256m -Xmx512m -Xmn128m

# Broker
environment:
  - JAVA_OPT=-Xms512m -Xmx1g -Xmn256m
```

如果仍然失败，可以进一步降低：

```yaml
# NameServer 极限配置
environment:
  - JAVA_OPT=-Xms128m -Xmx256m -Xmn64m

# Broker 极限配置
environment:
  - JAVA_OPT=-Xms256m -Xmx512m -Xmn128m
```

### 1.2 权限问题

**症状：**
```bash
# 日志显示
Permission denied
Cannot create directory /home/rocketmq/logs
```

**原因分析：**

RocketMQ 容器默认使用 `rocketmq` 用户（UID 3000），挂载目录权限不匹配。

**解决方案：**

```bash
# 方式一：修改挂载目录权限
sudo chmod -R 777 ./data/rocketmq

# 方式二：使用 root 用户运行（当前配置已设置）
# docker-compose-base.yml 中已添加:
user: root
```

### 1.3 网络问题

**症状：**
```bash
# Broker 无法连接 NameServer
connect to null failed
```

**原因分析：**

1. NameServer 未完全启动
2. 网络配置问题
3. 容器间网络隔离

**解决方案：**

```yaml
# 确保 depends_on 配置正确
depends_on:
  rocketmq-namesrv:
    condition: service_started  # 或 service_healthy

# 确保在同一网络
networks:
  - finrag4j-network
```

### 1.4 端口冲突

**症状：**
```bash
# 日志显示
Port 9876 already in use
Port 10911 already in use
```

**解决方案：**

```bash
# 检查端口占用
netstat -tlnp | grep 9876
netstat -tlnp | grep 10911

# 停止冲突服务
docker stop <冲突容器>

# 或修改端口映射
ports:
  - "9877:9876"  # 使用不同端口
```

### 1.5 虚拟机资源限制

**症状：**
```bash
# 容器频繁重启
# 日志无明显错误，但服务不稳定
```

**原因分析：**

虚拟机资源不足：
- CPU 核心数过少
- 内存不足
- 磁盘 I/O 性能差

**解决方案：**

```bash
# 检查虚拟机资源
free -h
df -h
nproc

# 建议最低配置
# CPU: 2核+
# 内存: 4GB+
# 磁盘: 20GB+
```

---

## 2. 排查步骤

### 2.1 查看容器状态

```bash
# 查看所有容器
docker ps -a | grep rocketmq

# 查看容器日志
docker logs finrag4j-rocketmq-namesrv
docker logs finrag4j-rocketmq-broker

# 实时查看日志
docker logs -f finrag4j-rocketmq-namesrv
```

### 2.2 检查健康状态

```bash
# 进入容器检查
docker exec -it finrag4j-rocketmq-namesrv bash

# 检查 Java 进程
ps aux | grep java

# 检查端口
netstat -tlnp

# 检查日志文件
cat /home/rocketmq/logs/namesrv.log
```

### 2.3 测试连接

```bash
# 测试 NameServer 端口
telnet 11.0.1.190 9876

# 测试 Broker 端口
telnet 11.0.1.190 10911

# 使用 Dashboard 检查
# 访问 http://11.0.1.190:8088
```

---

## 3. 完整解决方案

### 3.1 优化后的配置

创建 `rocketmq-optimized.yml`：

```yaml
version: '3.8'

services:
  # NameServer - 最小内存配置
  rocketmq-namesrv:
    image: apache/rocketmq:5.2.0
    container_name: finrag4j-rocketmq-namesrv
    command: sh mqnamesrv
    user: root
    environment:
      - JAVA_OPT_EXT=-Xms128m -Xmx256m -Xmn64m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m
    volumes:
      - ./data/rocketmq/namesrv/logs:/home/rocketmq/logs
    ports:
      - "9876:9876"
    networks:
      - finrag4j-network
    healthcheck:
      test: ["CMD-SHELL", "pgrep java || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s
    deploy:
      resources:
        limits:
          memory: 384M
        reservations:
          memory: 128M
    restart: unless-stopped

  # Broker - 最小内存配置
  rocketmq-broker:
    image: apache/rocketmq:5.2.0
    container_name: finrag4j-rocketmq-broker
    command: sh mqbroker -n rocketmq-namesrv:9876 --enable-proxy
    user: root
    environment:
      - JAVA_OPT_EXT=-Xms256m -Xmx512m -Xmn128m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m
    volumes:
      - ./data/rocketmq/broker/logs:/home/rocketmq/logs
      - ./data/rocketmq/broker/store:/home/rocketmq/store
    ports:
      - "10911:10911"
      - "10909:10909"
    networks:
      - finrag4j-network
    depends_on:
      rocketmq-namesrv:
        condition: service_healthy
    deploy:
      resources:
        limits:
          memory: 768M
        reservations:
          memory: 256M
    restart: unless-stopped

  # Dashboard
  rocketmq-dashboard:
    image: apacherocketmq/rocketmq-dashboard:latest
    container_name: finrag4j-rocketmq-dashboard
    environment:
      - NAMESRV_ADDR=rocketmq-namesrv:9876
      - JAVA_OPT=-Xmx256m
    ports:
      - "8088:8080"
    networks:
      - finrag4j-network
    depends_on:
      - rocketmq-namesrv
    restart: unless-stopped

networks:
  finrag4j-network:
    external: true
```

### 3.2 启动步骤

```bash
# 1. 创建数据目录并设置权限
mkdir -p ./data/rocketmq/{namesrv,broker}/{logs,store}
chmod -R 777 ./data/rocketmq

# 2. 确保网络存在
docker network create finrag4j-network 2>/dev/null || true

# 3. 启动 NameServer
docker-compose -f rocketmq-optimized.yml up -d rocketmq-namesrv

# 4. 等待 NameServer 完全启动
sleep 30
docker logs finrag4j-rocketmq-namesrv

# 5. 启动 Broker
docker-compose -f rocketmq-optimized.yml up -d rocketmq-broker

# 6. 等待 Broker 启动
sleep 30
docker logs finrag4j-rocketmq-broker

# 7. 启动 Dashboard
docker-compose -f rocketmq-optimized.yml up -d rocketmq-dashboard

# 8. 验证
docker-compose -f rocketmq-optimized.yml ps
```

### 3.3 验证成功

```bash
# 检查进程
docker exec finrag4j-rocketmq-namesrv pgrep java

# 检查端口
docker exec finrag4j-rocketmq-namesrv netstat -tlnp

# 访问 Dashboard
# http://11.0.1.190:8088
# 应该能看到集群信息
```

---

## 4. 替代方案

### 4.1 使用更轻量的消息队列

如果虚拟机资源实在有限，可以考虑：

**方案一：使用 Redis Stream**

```yaml
# Redis 已支持消息队列功能
# 无需额外部署 RocketMQ
services:
  redis:
    image: redis/redis-stack-server:latest
    ports:
      - "6379:6379"
```

**方案二：使用 Kafka（更轻量）**

```yaml
services:
  zookeeper:
    image: bitnami/zookeeper:latest
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
    ports:
      - "2181:2181"

  kafka:
    image: bitnami/kafka:latest
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper
```

### 4.2 使用外部 RocketMQ 服务

如果有可用的外部 RocketMQ：

```yaml
# 不部署本地 RocketMQ
# 直接配置外部地址
environment:
  - ROCKETMQ_NAMESRV_ADDR=外部IP:9876
```

---

## 5. 监控与维护

### 5.1 日志监控

```bash
# 实时监控日志
docker logs -f --tail 100 finrag4j-rocketmq-namesrv
docker logs -f --tail 100 finrag4j-rocketmq-broker

# 查看错误日志
docker exec finrag4j-rocketmq-namesrv grep -i error /home/rocketmq/logs/namesrv.log
```

### 5.2 资源监控

```bash
# 查看容器资源使用
docker stats finrag4j-rocketmq-namesrv
docker stats finrag4j-rocketmq-broker

# 查看虚拟机资源
free -h
df -h
```

### 5.3 定期维护

```bash
# 清理旧日志
docker exec finrag4j-rocketmq-broker rm -rf /home/rocketmq/logs/*.log.*

# 清理过期消息（谨慎操作）
# 通过 Dashboard 操作
```

---

## 6. 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 1 | 启动失败 | 检查日志，通常是内存不足 |
| 137 | OOM Killed | 增加内存限制 |
| 139 | Segmentation Fault | 检查 JVM 版本兼容性 |
| 143 | 被终止 | 检查是否被系统 OOM Killer 杀掉 |

---

## 7. 联系支持

如果以上方案都无法解决问题，请提供以下信息：

```bash
# 收集诊断信息
echo "=== Docker 版本 ===" > rocketmq-diag.txt
docker --version >> rocketmq-diag.txt

echo "=== 系统资源 ===" >> rocketmq-diag.txt
free -h >> rocketmq-diag.txt
df -h >> rocketmq-diag.txt
nproc >> rocketmq-diag.txt

echo "=== 容器状态 ===" >> rocketmq-diag.txt
docker ps -a | grep rocketmq >> rocketmq-diag.txt

echo "=== NameServer 日志 ===" >> rocketmq-diag.txt
docker logs finrag4j-rocketmq-namesrv >> rocketmq-diag.txt 2>&1

echo "=== Broker 日志 ===" >> rocketmq-diag.txt
docker logs finrag4j-rocketmq-broker >> rocketmq-diag.txt 2>&1
```

---

**文档版本**: v1.0.0
**更新日期**: 2026-06-19
