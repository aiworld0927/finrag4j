#!/bin/bash
# FinRag4j 数据目录初始化脚本
# 首次部署前必须执行此脚本创建数据目录

set -e

echo "=========================================="
echo "    FinRag4j 数据目录初始化"
echo "=========================================="

# 创建所有必要的数据目录
echo ""
echo "创建数据目录..."
mkdir -p ./data/{postgres,redis,minio,nacos,rocketmq/{namesrv,broker}}

# 设置目录权限
echo "设置目录权限..."
chmod -R 777 ./data

echo ""
echo "数据目录结构:"
find ./data -type d | head -20

echo ""
echo "=========================================="
echo "    数据目录创建完成！"
echo "=========================================="
echo ""
echo "现在可以执行以下命令启动服务:"
echo "  docker-compose -f docker-compose-base.yml up -d"
echo ""
