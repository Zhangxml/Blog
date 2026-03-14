#!/bin/bash
# OpenClaw DeepSeek 一键配置脚本
# 使用方法：./setup-deepseek.sh YOUR_DEEPSEEK_API_KEY

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}OpenClaw DeepSeek 配置工具${NC}"
echo -e "${BLUE}========================================${NC}"

# 检查参数
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}使用方法: $0 YOUR_DEEPSEEK_API_KEY${NC}"
    echo -e "${YELLOW}示例: $0 sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx${NC}"
    echo ""
    echo -e "${BLUE}如何获取DeepSeek API密钥:${NC}"
    echo "1. 访问 https://platform.deepseek.com"
    echo "2. 注册/登录账号"
    echo "3. 进入 API Keys 页面"
    echo "4. 创建新的API密钥"
    exit 1
fi

DEEPSEEK_API_KEY="$1"

echo -e "${GREEN}[1/5] 检查OpenClaw安装...${NC}"
if ! command -v openclaw &> /dev/null; then
    echo -e "${RED}错误: OpenClaw未安装${NC}"
    echo "请先安装OpenClaw: npm install -g openclaw"
    exit 1
fi

echo -e "${GREEN}[2/5] 创建配置文件...${NC}"
cp /home/zap/.openclaw/deepseek-config.json /home/zap/.openclaw/openclaw.json
echo "✅ 配置文件已创建"

echo -e "${GREEN}[3/5] 创建环境变量文件...${NC}"
cat > /home/zap/.openclaw/.env << EOF
# OpenClaw DeepSeek 配置
DEEPSEEK_API_KEY=${DEEPSEEK_API_KEY}

# 可选: 其他API密钥
# BRAVE_API_KEY=your_brave_key_for_web_search
# OPENAI_API_KEY=your_openai_key
# ANTHROPIC_API_KEY=your_anthropic_key
EOF
echo "✅ 环境变量文件已创建"

echo -e "${GREEN}[4/5] 创建启动脚本...${NC}"
cat > /home/zap/.openclaw/start.sh << 'EOF'
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
echo "模型: DeepSeek-V3"
EOF

chmod +x /home/zap/.openclaw/start.sh
echo "✅ 启动脚本已创建"

echo -e "${GREEN}[5/5] 启动OpenClaw网关...${NC}"
source /home/zap/.openclaw/.env
openclaw gateway restart

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}🎉 配置完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}📋 配置信息:${NC}"
echo "• API密钥: ${DEEPSEEK_API_KEY:0:8}...${DEEPSEEK_API_KEY: -4}"
echo "• 模型: DeepSeek-V3"
echo "• 控制面板: http://127.0.0.1:18789/"
echo "• 工作空间: /home/zap/.openclaw/workspace"
echo ""
echo -e "${YELLOW}🚀 启动命令:${NC}"
echo "  cd ~/.openclaw && ./start.sh"
echo ""
echo -e "${YELLOW}📝 常用命令:${NC}"
echo "  openclaw status          # 查看状态"
echo "  openclaw gateway status  # 查看网关状态"
echo "  openclaw logs --follow   # 查看日志"
echo ""
echo -e "${GREEN}现在可以开始使用OpenClaw了！${NC}"