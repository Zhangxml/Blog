#!/bin/bash
# OpenClaw启动脚本

# 加载环境变量
if [ -f "$HOME/.openclaw/.env" ]; then
    set -a
    source "$HOME/.openclaw/.env"
    set +a
    echo "✅ 环境变量已加载"
fi

# 检查网关状态
echo "🔍 检查OpenClaw网关状态..."
openclaw gateway status

# 如果网关未运行，则启动
if [ $? -ne 0 ]; then
    echo "🚀 启动OpenClaw网关..."
    openclaw gateway start
    sleep 2
    openclaw gateway status
fi

echo "🎉 OpenClaw已准备就绪！"
echo "控制面板: http://127.0.0.1:18789/"