@echo off
chcp 65001 >nul

echo 🌸 雲舟詞渡 - 水墨云图系统
echo ================================

REM 检查Node.js环境
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未安装Node.js，请先安装Node.js
    pause
    exit /b 1
)

REM 检查npm环境
npm --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未安装npm，请先安装npm
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('node --version') do set NODE_VERSION=%%i
for /f "tokens=*" %%i in ('npm --version') do set NPM_VERSION=%%i

echo ✅ Node.js 版本: %NODE_VERSION%
echo ✅ npm 版本: %NPM_VERSION%
echo.

REM 进入项目目录
cd /d "%~dp0"

REM 检查依赖
if not exist "node_modules" (
    echo 📦 正在安装依赖...
    npm install
    if errorlevel 1 (
        echo ❌ 依赖安装失败
        pause
        exit /b 1
    )
    echo ✅ 依赖安装成功
)

echo.
echo 🚀 正在启动开发服务器...
echo 📱 请在浏览器中访问以下地址：
echo.
echo    🏠 主界面: http://localhost:5173/inkcloud
echo    🧪 测试页面: http://localhost:5173/inkcloud-test
echo.
echo ⌨️  快捷键:
echo    空格键 - 切换导航
echo    Ctrl+C - 控制面板
echo    Ctrl+F - 全屏模式
echo.
echo 🎯 使用提示:
echo    • 移动鼠标体验水墨交互
echo    • 拖拽绘制毛笔轨迹
echo    • 点击印章打开导航
echo    • 在控制面板调节各种效果
echo.
echo 按 Ctrl+C 停止服务器
echo ================================

REM 启动开发服务器
npm run dev

pause
