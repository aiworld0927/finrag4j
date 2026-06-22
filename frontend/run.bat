@echo off
chcp 65001 >nul
echo ================================================
echo FinRag4j 前端开发服务器启动脚本
echo ================================================
echo.

echo 检查必要的后端服务...
echo - 网关服务 (localhost:8080)
echo - Python服务 (localhost:8002)
echo.

echo [1/2] 安装依赖(如果需要)...
cd /d "%~dp0..\frontend"
if not exist "node_modules" (
    echo 正在安装依赖...
    call npm install
)

echo.
echo [2/2] 启动开发服务器...
echo.
echo 启动后访问: http://localhost:5173
echo API代理到: http://localhost:8080/api
echo.
echo 按Ctrl+C停止服务器
echo.

call npm run dev

pause