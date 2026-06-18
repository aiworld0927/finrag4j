#!/bin/bash
# FinRag4j 启动脚本
# 支持 Ubuntu 24.04 LTS
# 使用方式:
#   ./start.sh [dev|test|prod]          # 启动全部服务
#   ./start.sh [dev|test|prod] base      # 仅启动基础中间件
#   ./start.sh [dev|test|prod] app       # 仅启动应用

set -e

# 检查 docker-compose
if ! command -v docker-compose &> /dev/null; then
    echo "错误: docker-compose 未安装"
    echo "安装命令: sudo apt update && sudo apt install -y docker.io docker-compose"
    exit 1
fi

# 解析环境参数
ENV_FILE=".env"
COMPONENT="all"

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
        echo "用法: $0 [dev|test|prod] [base|app]"
        exit 1
        ;;
esac

# 解析组件参数
if [ -n "$2" ]; then
    COMPONENT="$2"
fi

# 检查环境文件
if [ ! -f "$ENV_FILE" ]; then
    echo "错误: 环境配置文件 $ENV_FILE 不存在"
    exit 1
fi

echo "=========================================="
echo "    FinRag4j 服务启动"
echo "    环境: $ENV_FILE"
echo "    组件: $COMPONENT"
echo "    时间: $(date)"
echo "=========================================="

# 启动基础中间件
if [ "$COMPONENT" = "all" ] || [ "$COMPONENT" = "base" ]; then
    echo ""
    echo "[1/2] 启动基础中间件..."
    docker-compose -f docker-compose-base.yml --env-file "$ENV_FILE" up -d
    echo "基础中间件启动完成"
fi

# 启动应用服务
if [ "$COMPONENT" = "all" ] || [ "$COMPONENT" = "app" ]; then
    echo ""
    echo "[2/2] 启动应用服务..."
    docker-compose --env-file "$ENV_FILE" up -d
    echo "应用服务启动完成"
fi

echo ""
echo "=========================================="
echo "    服务启动成功！"
echo "=========================================="
echo ""
echo "服务端口映射:"
echo "  PostgreSQL 17: $(grep POSTGRES_PORT "$ENV_FILE" | cut -d'=' -f2):5432"
echo "  Redis Stack: $(grep REDIS_PORT "$ENV_FILE" | cut -d'=' -f2):6379"
echo "  MinIO: $(grep MINIO_PORT "$ENV_FILE" | cut -d'=' -f2):9000"
echo "  Python服务: $(grep PYTHON_SERVICE_PORT "$ENV_FILE" | cut -d'=' -f2):8001"
echo "  Java服务: $(grep JAVA_SERVICE_PORT "$ENV_FILE" | cut -d'=' -f2):8080"
echo "  Ollama: $(grep OLLAMA_PORT "$ENV_FILE" | cut -d'=' -f2):11434"
echo "  前端: $(grep FRONTEND_PORT "$ENV_FILE" | cut -d'=' -f2):80"
echo ""
echo "管理地址:"
echo "  前端界面: http://localhost:$(grep FRONTEND_PORT "$ENV_FILE" | cut -d'=' -f2)"
echo "  MinIO控制台: http://localhost:$(grep MINIO_CONSOLE_PORT "$ENV_FILE" | cut -d'=' -f2)"
echo "  Java API: http://localhost:$(grep JAVA_SERVICE_PORT "$ENV_FILE" | cut -d'=' -f2)/api"
echo ""
echo "查看日志:"
echo "  全部: docker-compose --env-file $ENV_FILE logs -f"
echo "  中间件: docker-compose -f docker-compose-base.yml --env-file $ENV_FILE logs -f"