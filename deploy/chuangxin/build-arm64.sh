# FinRag4j 国产信创适配构建脚本
# 支持ARM架构、麒麟JDK、鲲鹏处理器

#!/bin/bash

VERSION="1.0.0"
ARCH=$(uname -m)

echo "=========================================="
echo "FinRag4j 国产信创适配构建"
echo "架构: ${ARCH}"
echo "版本: ${VERSION}"
echo "=========================================="

# 检测系统类型
detect_os() {
    if [ -f /etc/kylin-release ]; then
        echo "kylin"
    elif [ -f /etc/uos-release ]; then
        echo "uos"
    elif [ -f /etc/centos-release ]; then
        echo "centos"
    elif [ -f /etc/ubuntu-release ]; then
        echo "ubuntu"
    else
        echo "unknown"
    fi
}

OS_TYPE=$(detect_os)
echo "操作系统: ${OS_TYPE}"

# 设置JDK路径
if [ "${ARCH}" = "aarch64" ]; then
    # ARM架构
    if [ "${OS_TYPE}" = "kylin" ]; then
        export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64
    elif [ "${OS_TYPE}" = "uos" ]; then
        export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64
    else
        export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
    fi
else
    export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
fi

echo "JAVA_HOME: ${JAVA_HOME}"
export PATH="${JAVA_HOME}/bin:${PATH}"

# 创建目录
mkdir -p build

# 构建Java服务
build_java_service() {
    echo "[1/4] 构建Java服务..."
    cd ../backend/finrag4j-java
    
    # 使用Maven构建
    mvn clean package -DskipTests -Pnative
    
    # 构建Docker镜像
    if [ "${ARCH}" = "aarch64" ]; then
        docker build -f Dockerfile.arm64 -t finrag4j/java-service:${VERSION}-arm64 .
        docker tag finrag4j/java-service:${VERSION}-arm64 finrag4j/java-service:${VERSION}
    else
        docker build -t finrag4j/java-service:${VERSION} .
    fi
    
    cd ../../deploy
}

# 构建Python服务
build_python_service() {
    echo "[2/4] 构建Python服务..."
    cd ../backend/finrag4j-python
    
    if [ "${ARCH}" = "aarch64" ]; then
        docker build -f Dockerfile.arm64 -t finrag4j/python-service:${VERSION}-arm64 .
        docker tag finrag4j/python-service:${VERSION}-arm64 finrag4j/python-service:${VERSION}
    else
        docker build -t finrag4j/python-service:${VERSION} .
    fi
    
    cd ../../deploy
}

# 构建前端
build_frontend() {
    echo "[3/4] 构建前端..."
    cd ../frontend
    
    npm install
    npm run build
    
    if [ "${ARCH}" = "aarch64" ]; then
        docker build -f Dockerfile.arm64 -t finrag4j/frontend:${VERSION}-arm64 .
        docker tag finrag4j/frontend:${VERSION}-arm64 finrag4j/frontend:${VERSION}
    else
        docker build -t finrag4j/frontend:${VERSION} .
    fi
    
    cd ../../deploy
}

# 下载基础镜像
pull_base_images() {
    echo "[4/4] 下载基础镜像..."
    
    # PostgreSQL 17 + PGVector
    if [ "${ARCH}" = "aarch64" ]; then
        docker pull pgvector/pgvector:pg17
    else
        docker pull pgvector/pgvector:pg17
    fi
    
    # Redis Stack Server
    docker pull redis/redis-stack-server:latest
    
    # MinIO
    docker pull minio/minio:latest
    
    # Ollama
    docker pull ollama/ollama:latest
    
    echo "基础镜像下载完成"
}

# 执行构建
build_java_service
build_python_service
build_frontend
pull_base_images

echo "=========================================="
echo "构建完成！"
echo "镜像列表:"
docker images | grep finrag4j
echo "=========================================="
echo "使用说明:"
echo "  启动服务: docker-compose --env-file .env up -d"
echo "  查看日志: docker-compose logs -f"