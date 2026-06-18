#!/bin/bash
# FinRag4j 停止脚本
# 使用方式:
#   ./stop.sh [dev|test|prod]          # 停止全部服务
#   ./stop.sh [dev|test|prod] base      # 仅停止基础中间件
#   ./stop.sh [dev|test|prod] app       # 仅停止应用

set -e

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

if [ -n "$2" ]; then
    COMPONENT="$2"
fi

if [ ! -f "$ENV_FILE" ]; then
    echo "错误: 环境配置文件 $ENV_FILE 不存在"
    exit 1
fi

echo "=========================================="
echo "    FinRag4j 服务停止"
echo "    环境: $ENV_FILE"
echo "    组件: $COMPONENT"
echo "    时间: $(date)"
echo "=========================================="

# 停止应用（先停止应用，避免依赖问题）
if [ "$COMPONENT" = "all" ] || [ "$COMPONENT" = "app" ]; then
    echo ""
    echo "停止应用服务..."
    docker-compose --env-file "$ENV_FILE" down
fi

# 停止基础中间件
if [ "$COMPONENT" = "all" ] || [ "$COMPONENT" = "base" ]; then
    echo ""
    echo "停止基础中间件..."
    docker-compose -f docker-compose-base.yml --env-file "$ENV_FILE" down
fi

echo ""
echo "服务已停止！"