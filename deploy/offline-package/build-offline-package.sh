#!/bin/bash

# FinRag4j 离线部署包生成脚本
# 适用于无外网环境的内网部署

set -e

VERSION="1.0.0"
PACKAGE_NAME="finrag4j-offline-${VERSION}"
BUILD_DIR="./build/${PACKAGE_NAME}"

echo "=========================================="
echo "FinRag4j 离线部署包生成脚本"
echo "版本: ${VERSION}"
echo "=========================================="

# 创建构建目录
rm -rf ${BUILD_DIR}
mkdir -p ${BUILD_DIR}
mkdir -p ${BUILD_DIR}/images
mkdir -p ${BUILD_DIR}/scripts
mkdir -p ${BUILD_DIR}/config
mkdir -p ${BUILD_DIR}/sql

# 1. 构建Docker镜像
echo "[1/6] 构建Docker镜像..."
cd ../backend/finrag4j-java
docker build -t finrag4j/java-service:${VERSION} .
docker save -o ../../deploy/${BUILD_DIR}/images/java-service.tar finrag4j/java-service:${VERSION}

cd ../finrag4j-python
docker build -t finrag4j/python-service:${VERSION} .
docker save -o ../../deploy/${BUILD_DIR}/images/python-service.tar finrag4j/python-service:${VERSION}

cd ../../frontend
docker build -t finrag4j/frontend:${VERSION} .
docker save -o ../deploy/${BUILD_DIR}/images/frontend.tar finrag4j/frontend:${VERSION}

# 2. 下载基础镜像
echo "[2/6] 下载基础镜像..."
cd ../deploy
docker pull pgvector/pgvector:pg17
docker save -o ${BUILD_DIR}/images/postgres.tar pgvector/pgvector:pg17

docker pull redis/redis-stack-server:latest
docker save -o ${BUILD_DIR}/images/redis.tar redis/redis-stack-server:latest

docker pull minio/minio:latest
docker save -o ${BUILD_DIR}/images/minio.tar minio/minio:latest

docker pull ollama/ollama:latest
docker save -o ${BUILD_DIR}/images/ollama.tar ollama/ollama:latest

# 3. 复制配置文件
echo "[3/6] 复制配置文件..."
cp docker-compose.yml ${BUILD_DIR}/
cp docker-compose-base.yml ${BUILD_DIR}/
cp .env ${BUILD_DIR}/
cp .env.dev ${BUILD_DIR}/
cp .env.test ${BUILD_DIR}/
cp .env.prod ${BUILD_DIR}/
cp -r k8s ${BUILD_DIR}/
cp -r ../backend/finrag4j-java/sql/*.sql ${BUILD_DIR}/sql/ 2>/dev/null || true

# 4. 创建启动脚本
echo "[4/6] 创建启动脚本..."
cat > ${BUILD_DIR}/start.sh << 'EOF'
#!/bin/bash
# FinRag4j 离线启动脚本

set -e

echo "=========================================="
echo "FinRag4j 离线部署启动脚本"
echo "=========================================="

# 加载Docker镜像
echo "[1/3] 加载Docker镜像..."
for image in images/*.tar; do
    echo "加载: $image"
    docker load -i "$image"
done

# 创建数据目录
echo "[2/3] 创建数据目录..."
mkdir -p /data/finrag4j/{postgres,redis,minio,ollama}

# 启动服务
echo "[3/3] 启动服务..."
docker-compose up -d

echo "=========================================="
echo "启动完成！"
echo "访问地址: http://localhost"
echo "API文档: http://localhost:8080/doc.html"
echo "MinIO控制台: http://localhost:9001"
echo "=========================================="
EOF

chmod +x ${BUILD_DIR}/start.sh

# 5. 创建停止脚本
echo "[5/6] 创建停止脚本..."
cat > ${BUILD_DIR}/stop.sh << 'EOF'
#!/bin/bash
# FinRag4j 离线停止脚本

echo "=========================================="
echo "FinRag4j 停止服务"
echo "=========================================="

docker-compose down

echo "服务已停止！"
EOF

chmod +x ${BUILD_DIR}/stop.sh

# 6. 打包
echo "[6/6] 打包..."
rm -f ${PACKAGE_NAME}.tar.gz
tar -czvf ${PACKAGE_NAME}.tar.gz -C ./build ${PACKAGE_NAME}

echo "=========================================="
echo "离线部署包生成完成！"
echo "文件: ${PACKAGE_NAME}.tar.gz"
echo "大小: $(du -h ${PACKAGE_NAME}.tar.gz | awk '{print $1}')"
echo "=========================================="
echo "使用说明:"
echo "  1. 将 ${PACKAGE_NAME}.tar.gz 传输到目标服务器"
echo "  2. 解压: tar -xzvf ${PACKAGE_NAME}.tar.gz"
echo "  3. 进入目录: cd ${PACKAGE_NAME}"
echo "  4. 启动: ./start.sh"
echo "  5. 停止: ./stop.sh"