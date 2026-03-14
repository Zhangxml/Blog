# OpenClaw DeepSeek 配置

## 🚀 快速开始

### 1. 获取DeepSeek API密钥
1. 访问 https://platform.deepseek.com
2. 注册/登录账号
3. 进入 **API Keys** 页面
4. 点击 **Create new API key**
5. 复制生成的密钥（以 `sk-` 开头）

### 2. 一键配置
```bash
cd ~/.openclaw
./setup-deepseek.sh sk-你的API密钥
```

### 3. 启动OpenClaw
```bash
cd ~/.openclaw
./start.sh
```

## 📁 文件结构
```
~/.openclaw/
├── openclaw.json          # 主配置文件
├── .env                   # 环境变量（包含API密钥）
├── setup-deepseek.sh      # 一键配置脚本
├── start.sh               # 启动脚本
├── deepseek-config.json   # 配置模板
└── workspace/             # 工作空间
```

## ⚙️ 配置详情

### 模型配置
- **提供商**: DeepSeek
- **模型**: DeepSeek-V3 (deepseek-chat)
- **上下文窗口**: 16,000 tokens
- **最大输出**: 4,096 tokens
- **成本**: 输入 ¥0.14/百万tokens，输出 ¥0.28/百万tokens

### 网关配置
- **端口**: 18789
- **控制面板**: http://127.0.0.1:18789/
- **模式**: 本地运行

## 🔧 常用命令

```bash
# 查看状态
openclaw status

# 查看网关状态
openclaw gateway status

# 启动网关
openclaw gateway start

# 停止网关
openclaw gateway stop

# 重启网关
openclaw gateway restart

# 查看日志
openclaw logs --follow

# 查看配置
openclaw config

# 修复配置
openclaw doctor --fix
```

## 🐛 故障排除

### 1. 网关启动失败
```bash
# 检查端口占用
netstat -tlnp | grep :18789

# 查看详细日志
journalctl --user -u openclaw-gateway.service -n 50 --no-pager
```

### 2. API密钥错误
```bash
# 重新设置API密钥
echo "DEEPSEEK_API_KEY=sk-你的新密钥" > ~/.openclaw/.env
openclaw gateway restart
```

### 3. 模型不可用
```bash
# 检查API密钥
echo $DEEPSEEK_API_KEY

# 测试API连接
curl -X POST https://api.deepseek.com/v1/chat/completions \
  -H "Authorization: Bearer $DEEPSEEK_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"deepseek-chat","messages":[{"role":"user","content":"Hello"}]}'
```

## 🔄 更新配置

### 添加联网搜索
1. 获取Brave Search API密钥：https://brave.com/search/api/
2. 编辑 `.env` 文件，添加：
   ```
   BRAVE_API_KEY=BSA-你的Brave密钥
   ```
3. 重启网关

### 切换模型
编辑 `openclaw.json` 中的：
```json
"agents": {
  "defaults": {
    "model": {
      "primary": "deepseek/deepseek-chat"  # 修改这里
    }
  }
}
```

## 📞 支持
- 官方文档：https://docs.openclaw.ai
- GitHub：https://github.com/openclaw/openclaw
- Discord：https://discord.com/invite/clawd