@echo off
chcp 65001 >nul
echo ============================================
echo          iwan Blog 开发环境一键启动脚本
echo ============================================
echo.

:: 检查 Docker 是否运行
echo [1/4] 检查 Docker 服务状态...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker 服务未运行，正在启动...
    net start docker >nul 2>&1
    if %errorlevel% neq 0 (
        echo 警告：Docker 服务启动失败，请手动启动 Docker Desktop
    ) else (
        echo Docker 服务启动成功
        timeout /t 5 /nobreak >nul
    )
) else (
    echo Docker 服务已运行
)

:: 启动 Docker 容器（PostgreSQL、Redis、RabbitMQ）
echo.
echo [2/4] 启动 Docker 容器...
cd /d "%~dp0"
docker-compose up -d
if %errorlevel% equ 0 (
    echo Docker 容器启动成功
    echo 等待容器初始化...
    timeout /t 10 /nobreak >nul
) else (
    echo 警告：Docker 容器启动失败
)

:: 启动后端服务
echo.
echo [3/4] 启动后端服务...
start "iwan Backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"
echo 后端服务已启动（端口：8080）

:: 启动前端服务
echo.
echo [4/4] 启动前端服务...
start "iwan Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"
echo 前端服务已启动（端口：5173）

echo.
echo ============================================
echo           开发环境启动完成！
echo ============================================
echo 前端地址: http://localhost:5173
echo 后端API:  http://localhost:8080/iwan/api/v1
echo.
echo 按任意键打开前端页面...
pause >nul
start http://localhost:5173
