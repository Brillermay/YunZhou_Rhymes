#!/bin/bash

# 🎨 水墨云图系统快速启动脚本

echo "🌸 雲舟詞渡 - 水墨云图系统"
echo "================================"

# 检查Node.js环境
if ! command -v node &> /dev/null; then
    echo "❌ 错误：未安装Node.js，请先安装Node.js"
    exit 1
fi

# 检查npm环境
if ! command -v npm &> /dev/null; then
    echo "❌ 错误：未安装npm，请先安装npm"
    exit 1
fi

echo "✅ Node.js 版本: $(node --version)"
echo "✅ npm 版本: $(npm --version)"
echo ""

# 进入项目目录
cd "$(dirname "$0")"

# 检查依赖
if [ ! -d "node_modules" ]; then
    echo "📦 正在安装依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败"
        exit 1
    fi
    echo "✅ 依赖安装成功"
fi

echo ""
echo "🚀 正在启动开发服务器..."
echo "📱 请在浏览器中访问以下地址："
echo ""
echo "   🏠 主界面: http://localhost:5173/inkcloud"
echo "   🧪 测试页面: http://localhost:5173/inkcloud-test"
echo ""
echo "⌨️  快捷键:"
echo "   空格键 - 切换导航"
echo "   Ctrl+C - 控制面板"
echo "   Ctrl+F - 全屏模式"
echo ""
echo "🎯 使用提示:"
echo "   • 移动鼠标体验水墨交互"
echo "   • 拖拽绘制毛笔轨迹"
echo "   • 点击印章打开导航"
echo "   • 在控制面板调节各种效果"
echo ""
echo "按 Ctrl+C 停止服务器"
echo "================================"

# 启动开发服务器
npm run dev
