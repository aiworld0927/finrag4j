@echo off
chcp 65001 >nul
echo ================================================
echo FinRag4j 后端构建脚本
echo ================================================
echo.

set JAVA_HOME=C:\dev\Java\jdk-21.0.11
set MAVEN_HOME=C:\dev\apache-maven-3.9.9
set MAVEN_CONF=%MAVEN_HOME%\conf\settings.xml

echo [1/4] 检查Java环境...
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo 错误: 未找到Java JDK at %JAVA_HOME%
    echo 请检查 JAVA_HOME 路径配置
    exit /b 1
)
echo Java版本:
"%JAVA_HOME%\bin\java" -version

echo.
echo [2/4] 检查Maven环境...
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo 错误: 未找到Maven at %MAVEN_HOME%
    echo 请检查 MAVEN_HOME 路径配置
    exit /b 1
)
echo Maven版本:
"%MAVEN_HOME%\bin\mvn" -version

echo.
echo [3/4] 清理并编译项目...
cd /d "%~dp0.."
call "%MAVEN_HOME%\bin\mvn.cmd" clean compile -s "%MAVEN_CONF%" -DskipTests

if %ERRORLEVEL% neq 0 (
    echo 编译失败!
    exit /b 1
)

echo.
echo [4/4] 打包项目...
call "%MAVEN_HOME%\bin\mvn.cmd" package -s "%MAVEN_CONF%" -DskipTests

if %ERRORLEVEL% neq 0 (
    echo 打包失败!
    exit /b 1
)

echo.
echo ================================================
echo 构建成功!
echo JAR文件位于: target\*.jar
echo ================================================