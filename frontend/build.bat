@echo off
chcp 65001 >nul
echo ================================================
echo FinRag4j 前端构建脚本
echo ================================================
echo.

echo [1/3] 检查Node.js环境...
where node >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo 错误: 未找到Node.js，请先安装Node.js
    exit /b 1
)
echo Node版本:
node -v
echo npm版本:
npm -v

echo.
echo [2/3] 安装依赖...
cd /d "%~dp0..\frontend"
call npm install

echo.
echo [3/3] 构建项目...
call npm run build

if %ERRORLEVEL% neq 0 (
    echo 构建失败!
    exit /b 1
)

echo.
echo ================================================
echo 构建成功!
echo 构建文件位于: dist\
echo ================================================