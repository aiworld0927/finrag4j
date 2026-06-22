@echo off
chcp 65001 >nul
echo ================================================
echo FinRag4j 后端服务启动脚本
echo ================================================
echo.

set JAVA_HOME=C:\dev\Java\jdk-21.0.11
set MAVEN_HOME=C:\dev\apache-maven-3.9.9
set MAVEN_CONF=%MAVEN_HOME%\conf\settings.xml

echo 检查必要的中间件服务...
echo - Nacos (localhost:8848)
echo - PostgreSQL (localhost:5432)
echo - Redis (localhost:6379)
echo - MinIO (localhost:9000)
echo.

echo 按Ctrl+C取消启动，或按任意键继续...
pause >nul

cd /d "%~dp0"

echo.
echo 启动服务...
echo.

REM 启动网关服务 (端口8080)
start "FinRag4j-Gateway" cmd /c "cd %cd%\finrag4j-gateway && ..\..\..\..\..\..\..\..\..\%JAVA_HOME%\bin\java -jar target\finrag4j-gateway-*.jar"

REM 启动认证服务 (端口8081)
start "FinRag4j-Auth" cmd /c "cd %cd%\finrag4j-auth && ..\..\..\..\..\..\..\..\..\%JAVA_HOME%\bin\java -jar target\finrag4j-auth-*.jar"

REM 启动文档服务 (端口8082)
start "FinRag4j-Document" cmd /c "cd %cd%\finrag4j-document && ..\..\..\..\..\..\..\..\..\%JAVA_HOME%\bin\java -jar target\finrag4j-document-*.jar"

REM 启动搜索服务 (端口8083)
start "FinRag4j-Search" cmd /c "cd %cd%\finrag4j-search && ..\..\..\..\..\..\..\..\..\%JAVA_HOME%\bin\java -jar target\finrag4j-search-*.jar"

REM 启动Agent服务 (端口8084)
start "FinRag4j-Agent" cmd /c "cd %cd%\finrag4j-agent && ..\..\..\..\..\..\..\..\..\%JAVA_HOME%\bin\java -jar target\finrag4j-agent-*.jar"

echo.
echo 服务已启动!
echo - 网关: http://localhost:8080
echo - Swagger文档: http://localhost:8080/docs
echo.
echo 按任意键退出此窗口(服务将继续运行)...
pause >nul