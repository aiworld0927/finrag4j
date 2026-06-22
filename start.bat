@echo off
chcp 65001 >nul
echo ================================================
echo FinRag4j 一键启动脚本
echo ================================================
echo.

set JAVA_HOME=C:\dev\Java\jdk-21.0.11
set MAVEN_HOME=C:\dev\apache-maven-3.9.9

:: 设置Nacos配置
set NACOS_HOST=11.0.1.190
set NACOS_PORT=8848

echo 检查必要的中间件服务...
echo - Nacos: http://%NACOS_HOST%:%NACOS_PORT%
echo - PostgreSQL (localhost:5432)
echo - Redis (localhost:6379)
echo - MinIO (localhost:9000)
echo.

echo [1/3] 启动Python预处理服务...
start "FinRag4j-Python" cmd /c "cd /d %~dp0backend\finrag4j-python && set NACOS_HOST=%NACOS_HOST% && set NACOS_PORT=%NACOS_PORT% && .venv\Scripts\python.exe main.py"

timeout /t 5 /nobreak >nul

echo [2/3] 启动Java后端服务...
cd /d "%~dp0backend"

:: 启动网关服务 (端口8080)
start "FinRag4j-Gateway" cmd /c "cd %cd%\finrag4j-gateway && set NACOS_HOST=%NACOS_HOST% && set NACOS_PORT=%NACOS_PORT% && %JAVA_HOME%\bin\java -jar target\finrag4j-gateway-*.jar"

timeout /t 3 /nobreak >nul

:: 启动认证服务
start "FinRag4j-Auth" cmd /c "cd %cd%\finrag4j-auth && set NACOS_HOST=%NACOS_HOST% && set NACOS_PORT=%NACOS_PORT% && %JAVA_HOME%\bin\java -jar target\finrag4j-auth-*.jar"

:: 启动文档服务
start "FinRag4j-Document" cmd /c "cd %cd%\finrag4j-document && set NACOS_HOST=%NACOS_HOST% && set NACOS_PORT=%NACOS_PORT% && %JAVA_HOME%\bin\java -jar target\finrag4j-document-*.jar"

:: 启动搜索服务
start "FinRag4j-Search" cmd /c "cd %cd%\finrag4j-search && set NACOS_HOST=%NACOS_HOST% && set NACOS_PORT=%NACOS_PORT% && %JAVA_HOME%\bin\java -jar target\finrag4j-search-*.jar"

:: 启动Agent服务
start "FinRag4j-Agent" cmd /c "cd %cd%\finrag4j-agent && set NACOS_HOST=%NACOS_HOST% && set NACOS_PORT=%NACOS_PORT% && %JAVA_HOME%\bin\java -jar target\finrag4j-agent-*.jar"

timeout /t 5 /nobreak >nul

echo [3/3] 启动前端开发服务器...
cd /d "%~dp0frontend"

if not exist "node_modules" (
    echo 正在安装前端依赖...
    call npm install
)

:: 设置代理地址为网关
set VITE_API_URL=http://11.0.1.190:8080/api
start "FinRag4j-Frontend" cmd /c "npm run dev"

echo.
echo ================================================
echo FinRag4j 服务启动完成!
echo ================================================
echo.
echo 访问地址:
echo - 前端: http://localhost:5173
echo - 网关: http://localhost:8080
echo - Swagger: http://localhost:8080/docs
echo - Python: http://localhost:8002
echo - Nacos: http://%NACOS_HOST%:8080/nacos
echo.
echo 所有服务已在后台运行
echo 按任意键退出此窗口...
pause >nul