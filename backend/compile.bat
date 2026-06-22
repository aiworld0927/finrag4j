@echo off
chcp 65001 >nul

:: 设置Java和Maven路径
set JAVA_HOME=C:\dev\Java\jdk-21.0.11
set MAVEN_HOME=C:\dev\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo ================================================
echo FinRag4j 后端编译脚本
echo ================================================
echo Java Home: %JAVA_HOME%
echo Maven Home: %MAVEN_HOME%
echo.

:: 验证Java版本
echo 验证Java版本...
java -version
echo.

:: 编译项目
echo 开始编译项目...
cd /d "%~dp0"
call mvn clean package -DskipTests

if %ERRORLEVEL% equ 0 (
    echo.
    echo ================================================
    echo 编译成功!
    echo ================================================
) else (
    echo.
    echo ================================================
    echo 编译失败!
    echo ================================================
    exit /b 1
)