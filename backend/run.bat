@echo off
chcp 65001 >nul
echo ================================================
echo FinRag4j 后端服务启动脚本
echo ================================================
echo.

set JAVA_HOME=C:\dev\Java\jdk-21.0.11

echo 配置环境变量...
set NACOS_HOST=11.0.1.190
set NACOS_PORT=8848
set NACOS_NAMESPACE=public
set NACOS_USERNAME=nacos
set NACOS_PASSWORD=nacos
set NACOS_AUTH_ENABLE=true
set NACOS_AUTH_IDENTITY_KEY=serverIdentity
set NACOS_AUTH_IDENTITY_VALUE=mySecureValue123

set POSTGRES_HOST=11.0.1.190
set POSTGRES_PORT=5432
set POSTGRES_DB=nacos
set POSTGRES_USER=nacos
set POSTGRES_PASSWORD=nacos

set REDIS_HOST=11.0.1.190
set REDIS_PORT=6379
set REDIS_PASSWORD=

set MINIO_HOST=11.0.1.190
set MINIO_PORT=9000
set MINIO_ROOT_USER=minioadmin
set MINIO_ROOT_PASSWORD=minioadmin

set GATEWAY_PORT=8085
set AUTH_PORT=8081
set DOCUMENT_PORT=9082
set SEARCH_PORT=8083
set AGENT_PORT=8086

echo.
echo ================================================
echo 中间件配置:
echo - Nacos: %NACOS_HOST%:%NACOS_PORT%
echo - PostgreSQL: %POSTGRES_HOST%:%POSTGRES_PORT%/%POSTGRES_DB%
echo - Redis: %REDIS_HOST%:%REDIS_PORT%
echo - MinIO: %MINIO_HOST%:%MINIO_PORT%
echo ================================================
echo.

cd /d "%~dp0"

echo 正在启动服务...
echo.

REM 启动网关服务 (端口8085)
echo [1/5] 启动网关服务...
start "FinRag4j-Gateway" cmd /c "cd /d %cd%\finrag4j-gateway && %JAVA_HOME%\bin\java -Dserver.port=%GATEWAY_PORT% -jar target\finrag4j-gateway-1.0.0.jar --spring.cloud.nacos.config.import-check.enabled=false"
timeout /t 3 /nobreak >nul

REM 启动认证服务 (端口8081)
echo [2/5] 启动认证服务...
start "FinRag4j-Auth" cmd /c "cd /d %cd%\finrag4j-auth && %JAVA_HOME%\bin\java -Dserver.port=%AUTH_PORT% -jar target\finrag4j-auth-1.0.0.jar --spring.cloud.nacos.config.import-check.enabled=false"
timeout /t 3 /nobreak >nul

REM 启动文档服务 (端口8082)
echo [3/5] 启动文档服务...
start "FinRag4j-Document" cmd /c "cd /d %cd%\finrag4j-document && %JAVA_HOME%\bin\java -Dserver.port=%DOCUMENT_PORT% -jar target\finrag4j-document-1.0.0.jar --spring.cloud.nacos.config.import-check.enabled=false"
timeout /t 3 /nobreak >nul

REM 启动搜索服务 (端口8083)
echo [4/5] 启动搜索服务...
start "FinRag4j-Search" cmd /c "cd /d %cd%\finrag4j-search && %JAVA_HOME%\bin\java -Dserver.port=%SEARCH_PORT% -jar target\finrag4j-search-1.0.0.jar --spring.cloud.nacos.config.import-check.enabled=false"
timeout /t 3 /nobreak >nul

REM 启动Agent服务 (端口8086)
echo [5/5] 启动Agent服务...
start "FinRag4j-Agent" cmd /c "cd /d %cd%\finrag4j-agent && %JAVA_HOME%\bin\java -Dserver.port=%AGENT_PORT% -jar target\finrag4j-agent-1.0.0.jar --spring.cloud.nacos.config.import-check.enabled=false"

echo.
echo ================================================
echo 服务启动完成!
echo ================================================
echo.
echo 服务地址:
echo - 网关: http://localhost:%GATEWAY_PORT%
echo - 认证服务: http://localhost:%AUTH_PORT%
echo - 文档服务: http://localhost:%DOCUMENT_PORT%
echo - 搜索服务: http://localhost:%SEARCH_PORT%
echo - Agent服务: http://localhost:%AGENT_PORT%
echo.
echo 前端开发服务器代理配置:
echo - Vite代理: http://11.0.1.190:%GATEWAY_PORT%
echo.
echo 按任意键退出此窗口(服务将继续在后台运行)...
pause >nul
