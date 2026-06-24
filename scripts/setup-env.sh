#!/bin/bash

# ============================================
# FinRag4j 开发环境变量配置脚本
# ============================================

# 设置默认环境（可通过参数覆盖）
# 用法: source setup-env.sh devhome | devcompany
ENV_NAME=${1:-devhome}

# ============================================
# 0. 环境选择
# ============================================
echo "================================================"
echo "FinRag4j 开发环境变量配置脚本"
echo "当前环境: $ENV_NAME"
echo "================================================"

# 根据环境选择中间件地址
case $ENV_NAME in
    devhome)
        MIDDLEWARE_HOST=11.0.1.190
        NACOS_PORT=8848
        ;;
    devcompany)
        MIDDLEWARE_HOST=192.168.222.188
        NACOS_PORT=8848
        ;;
    *)
        echo "未知环境: $ENV_NAME，使用默认值"
        MIDDLEWARE_HOST=localhost
        NACOS_PORT=8848
        ;;
esac

# ============================================
# 1. 基础环境配置
# ============================================
echo ""
echo "[1/6] 配置基础环境变量..."

# Java 21 配置 (与项目其他配置保持一致)
export JAVA_HOME=/usr/local/java/jdk-21.0.11
export PATH=$JAVA_HOME/bin:$PATH

# Maven 配置 (与项目其他配置保持一致)
export MAVEN_HOME=/coding/maven/apache-maven-3.9.16
export PATH=$MAVEN_HOME/bin:$PATH

echo "  ✅ JAVA_HOME: $JAVA_HOME"
echo "  ✅ MAVEN_HOME: $MAVEN_HOME"

# ============================================
# 2. 数据库配置 (PostgreSQL)
# ============================================
echo ""
echo "[2/6] 配置数据库环境变量..."

export POSTGRES_HOST=$MIDDLEWARE_HOST
export POSTGRES_PORT=5432
export POSTGRES_USER=nacos
export POSTGRES_PASSWORD=nacos
export POSTGRES_DB=nacos

echo "  ✅ POSTGRES_HOST: $POSTGRES_HOST"
echo "  ✅ POSTGRES_PORT: $POSTGRES_PORT"
echo "  ✅ POSTGRES_USER: $POSTGRES_USER"

# ============================================
# 3. 中间件配置
# ============================================
echo ""
echo "[3/6] 配置中间件环境变量..."

# Redis
export REDIS_HOST=$MIDDLEWARE_HOST
export REDIS_PORT=6379
export REDIS_PASSWORD=

echo "  ✅ REDIS_HOST: $REDIS_HOST"
echo "  ✅ REDIS_PORT: $REDIS_PORT"

# Nacos
export NACOS_HOST=$MIDDLEWARE_HOST
export NACOS_PORT=$NACOS_PORT
export NACOS_NAMESPACE=public
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=nacos
# Nacos 3.x 鉴权配置（必须）
export NACOS_AUTH_ENABLE=true
export NACOS_AUTH_IDENTITY_KEY=serverIdentity
export NACOS_AUTH_IDENTITY_VALUE=mySecureValue123
export NACOS_AUTH_TOKEN=VGhpc0lzTXlTZWNyZXRLZXlXaGljaFNob3VsZEJlMzJDaGFyYWN0ZXJzTGFyZ2U=

echo "  ✅ NACOS_HOST: $NACOS_HOST"
echo "  ✅ NACOS_PORT: $NACOS_PORT"
echo "  ✅ NACOS_NAMESPACE: $NACOS_NAMESPACE"
echo "  ✅ NACOS_AUTH_ENABLE: $NACOS_AUTH_ENABLE"

# MinIO
export MINIO_HOST=$MIDDLEWARE_HOST
export MINIO_ENDPOINT=http://$MINIO_HOST:9000
export MINIO_PORT=9000
export MINIO_CONSOLE_PORT=9001
export MINIO_ACCESS_KEY=minioadmin
export MINIO_SECRET_KEY=minioadmin

echo "  ✅ MINIO_ENDPOINT: $MINIO_ENDPOINT"

# RocketMQ
export ROCKETMQ_HOST=$MIDDLEWARE_HOST
export ROCKETMQ_NAMESRV_PORT=9876
export ROCKETMQ_BROKER_PORT=10911
export ROCKETMQ_BROKER_PORT2=10909

echo "  ✅ ROCKETMQ_HOST: $ROCKETMQ_HOST"

# ============================================
# 4. AI 模型配置
# ============================================
echo ""
echo "[4/6] 配置AI模型环境变量..."

# LLM 配置 (Ollama)
export LLM_BASE_URL=http://localhost:11434
export LLM_API_KEY=ollama
export LLM_MODEL_NAME=qwen2.5

echo "  ✅ LLM_BASE_URL: $LLM_BASE_URL"
echo "  ✅ LLM_MODEL_NAME: $LLM_MODEL_NAME"

# ModelScope 配置 (可选，取消注释启用)
# export MODELSCOPE_API_KEY=your-modelscope-api-key
# export MODELSCOPE_ENDPOINT=https://api.modelscope.cn
# echo "  ✅ ModelScope 配置已启用"

# ============================================
# 5. 服务端口配置
# ============================================
echo ""
echo "[5/6] 配置服务端口..."

export GATEWAY_PORT=8085
export AUTH_PORT=8081
export DOCUMENT_PORT=9082
export SEARCH_PORT=8083
export AGENT_PORT=8086
export PYTHON_SERVICE_PORT=8001
export FRONTEND_PORT=80

echo "  ✅ GATEWAY_PORT: $GATEWAY_PORT"
echo "  ✅ AUTH_PORT: $AUTH_PORT"
echo "  ✅ DOCUMENT_PORT: $DOCUMENT_PORT"
echo "  ✅ SEARCH_PORT: $SEARCH_PORT"
echo "  ✅ AGENT_PORT: $AGENT_PORT"

# ============================================
# 6. 应用配置
# ============================================
echo ""
echo "[6/6] 配置应用环境变量..."

# JWT 密钥
export JWT_SECRET=finrag4j-secret-key-change-in-production

# Python 服务
export PYTHON_SERVICE_URL=http://$MIDDLEWARE_HOST:$PYTHON_SERVICE_PORT

# 服务地址（根据中间件主机动态生成）
export NACOS_SERVER_ADDR=$NACOS_HOST:$NACOS_PORT
export POSTGRES_URL=jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/$POSTGRES_DB

echo "  ✅ JWT_SECRET: 已配置"
echo "  ✅ PYTHON_SERVICE_URL: $PYTHON_SERVICE_URL"
echo "  ✅ NACOS_SERVER_ADDR: $NACOS_SERVER_ADDR"

# ============================================
# 完成提示
# ============================================
echo ""
echo "================================================"
echo "环境变量配置完成！"
echo "================================================"
echo ""
echo "当前环境: $ENV_NAME"
echo "中间件地址: $MIDDLEWARE_HOST"
echo ""
echo "使用方式："
echo "  1. 执行 source setup-env.sh devhome        # 家庭环境"
echo "  2. 执行 source setup-env.sh devcompany    # 公司环境"
echo "  3. 或添加到 ~/.bashrc 或 ~/.zshrc 中"
echo ""
echo "验证配置："
echo "  java -version         # 验证Java版本"
echo "  mvn -v                # 验证Maven版本"
echo "  echo \$NACOS_HOST     # 验证Nacos地址"
echo "  echo \$POSTGRES_HOST  # 验证数据库地址"
echo ""
echo "注意：生产环境请修改以下敏感配置："
echo "  - POSTGRES_PASSWORD"
echo "  - REDIS_PASSWORD"
echo "  - NACOS_PASSWORD"
echo "  - MINIO_ACCESS_KEY / MINIO_SECRET_KEY"
echo "  - JWT_SECRET"
echo "  - NACOS_AUTH_TOKEN"
echo "================================================"
