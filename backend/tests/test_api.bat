@echo off
chcp 65001 >nul
echo ================================================
echo FinRag4j API接口测试脚本
echo ================================================
echo.

set BASE_URL=http://localhost:8080
set PYTHON_URL=http://localhost:8002
set TOKEN=

echo [1/5] 测试认证管理模块
echo ------------------------

rem 测试登录
echo 测试用户登录...
for /f "tokens=*" %%a in ('curl -s -X POST %BASE_URL%/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"') do (
    set RESPONSE=%%a
)
echo 响应: %RESPONSE%
echo.

echo [2/5] 测试文档管理模块
echo ------------------------

rem 测试查询知识库
echo 测试查询知识库...
curl -s -X GET %BASE_URL%/knowledge-base
echo.
echo.

echo [3/5] 测试搜索检索模块
echo ------------------------

rem 测试语义搜索
echo 测试语义搜索...
curl -s -X POST %BASE_URL%/rag/search -H "Content-Type: application/json" -d "{\"query\":\"测试\",\"topK\":5}"
echo.
echo.

echo [4/5] 测试智能代理模块
echo ------------------------

rem 测试创建会话
echo 测试创建会话...
curl -s -X POST %BASE_URL%/chat/session/create -H "Content-Type: application/json" -d "{\"title\":\"测试会话\",\"agentType\":\"rag\"}"
echo.
echo.

echo [5/5] 测试Python预处理模块
echo ------------------------

rem 测试健康检查
echo 测试Python服务健康检查...
curl -s -X GET %PYTHON_URL%/health
echo.
echo.

echo ================================================
echo 测试完成
echo ================================================