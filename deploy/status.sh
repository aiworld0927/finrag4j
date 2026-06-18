#!/bin/bash
# FinRag4j 状态检查脚本
# 使用方式: ./status.sh [dev|test|prod]

set -e

# 解析环境参数
ENV_FILE=".env"

case "$1" in
    dev)
        ENV_FILE=".env.dev"
        ;;
    test)
        ENV_FILE=".env.test"
        ;;
    prod)
        ENV_FILE=".env.prod"
        ;;
    "")
        ;;
    *)
        echo "用法: $0 [dev|test|prod]"
        exit 1
        ;;
esac

if [ ! -f "$ENV_FILE" ]; then
    echo "错误: 环境配置文件 $ENV_FILE 不存在"
    exit 1
fi

echo "=========================================="
echo "    FinRag4j 服务状态检查"
echo "    环境: $ENV_FILE"
echo "    时间: $(date)"
echo "=========================================="

echo ""
echo "========== 全部服务 =========="
docker-compose --env-file "$ENV_FILE" ps

echo ""
echo "------------------------------------------"
echo "服务端口映射:"
echo "  PostgreSQL 17: $(grep POSTGRES_PORT "$ENV_FILE" | cut -d'=' -f2):5432"
echo "  Redis Stack: $(grep REDIS_PORT "$ENV_FILE" | cut -d'=' -f2):6379"
echo "  MinIO: $(grep MINIO_PORT "$ENV_FILE" | cut -d'=' -f2):9000"
echo "  Python服务: $(grep PYTHON_SERVICE_PORT "$ENV_FILE" | cut -d'=' -f2):8001"
echo "  Java服务: $(grep JAVA_SERVICE_PORT "$ENV_FILE" | cut -d'=' -f2):8080"
echo "  Ollama: $(grep OLLAMA_PORT "$ENV_FILE" | cut -d'=' -f2):11434"
echo "  前端: $(grep FRONTEND_PORT "$ENV_FILE" | cut -d'=' -f2):80"