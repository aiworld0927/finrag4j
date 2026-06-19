# FinRag4j Python 预处理微服务 Dockerfile
# Python 3.12+

FROM python:3.12-slim

# 设置工作目录
WORKDIR /app

# 安装系统依赖
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgl1-mesa-glx \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# 安装 uv
RUN pip install --no-cache-dir uv

# 复制项目文件
COPY pyproject.toml uv.lock* ./

# 使用 uv 安装依赖
RUN uv sync --no-dev

# 复制源代码
COPY . .

# 创建必要目录
RUN mkdir -p logs temp

# 暴露端口
EXPOSE 8001

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8001/health || exit 1

# 启动命令
CMD ["uv", "run", "python", "main.py"]
