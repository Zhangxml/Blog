# ubuntu20.04配置 openclaw



### 第一步：配置 Ubuntu 系统环境

打开你的终端，按顺序执行以下命令。

1. **更新软件源和系统**（确保系统包是最新的）:

   bash

   ```
   sudo apt update && sudo apt upgrade -y
   ```

   

2. **安装基础依赖工具**（编译和下载时需要）:

   bash

   ```
   sudo apt install -y curl wget git python3 build-essential libssl-dev ufw
   ```

   

3. **安装 Node.js**（OpenClaw 的核心依赖，**必须安装 v22 或更高版本**）:

   bash

   ```
   # 导入 NodeSource 官方源，安装 Node.js 22.x
   curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
   sudo apt install -y nodejs
   
   # 验证安装版本，确认输出是 v22.x.x
   node --version
   npm --version
   ```

   

4. **优化国内下载速度**（配置 npm 国内镜像，这一步很重要，可以避免卡顿）:

   bash

   ```
   npm config set registry https://registry.npmmirror.com
   ```

   

### 第二步：安装 OpenClaw

使用npm进行全局安装，这是最直接、最灵活的方式。

1. **执行安装命令**:

   bash

   ```
   npm install -g openclaw@latest
   ```

   安装过程可能需要几分钟，请耐心等待。

2. **验证安装**:

   bash

   ```
   openclaw --version
   ```

   如果显示出版本号（例如 `2026.2.9`），说明安装成功



### 第三步：配置大脑（大模型）

openclaw只是工具，需要和大模型配置使用。两者的区别简单来说：**普通 AI 只能"说"，OpenClaw 能"做"！**

1.**启动**:

bash

```
openclaw gateway start
```

此时应该会报错，因为没有环境。继续往下执行：

执行以下命令，将网关安装为 systemd 用户服务：

bash

```
openclaw gateway install
```

安装完成后，你就可以用下面任一命令启动它：

bash

```
openclaw gateway start
# 或
systemctl --user start openclaw-gateway.service
```

> ps：
>
> 开机自启
>
> ```
> systemctl --user enable openclaw-gateway.service
> ```
>
> 禁用开机自启
>
> ```
> systemctl --user disable openclaw-gateway.service
> ```
>
> 验证已禁用
>
> ```
> systemctl --user is-enabled openclaw-gateway.service
> ```

应该还是会报错，OpenClaw 需要知道以什么模式运行（比如本地模式还是联网模式），以及连接哪个AI大脑，所以需要配置大脑，继续往下：

- [ ] **运行配置向导**

执行以下命令，完成基础配置：

```
openclaw onboard
```

- [ ] **选择提供商**

1. 当询问"选择模型提供商"时，由于DeepSeek不在内置列表里，请选择 **OpenAI** 或 **Custom (OpenAI Compatible)
2. **Base URL**: 填入 **`https://api.deepseek.com`** （注意末尾不要加 `/v1`）。
3. **API Key**: 填入你从DeepSeek官网复制的、以 `sk-` 开头的密钥。
4. **Model Name**: 这里最关键！**请输入 `deepseek-chat`**（如果你想用默认模型）或 `deepseek-reasoner`（如果你想用推理模型），**千万不要加任何前缀**

由**系统自动生成**配置即可，需要注意的是：确保配置大致如下（以添加一个名为 `custom` 的提供商为例）：

```
{
  "models": {
    "providers": {
      "deepseek": { // 也可以是 "deepseek" 或其它名字
        "baseUrl": "https://api.deepseek.com",
        "apiKey": "sk-你的API密钥",
        "api": "openai-completions", // DeepSeek 兼容 OpenAI 协议
        "models": [
          {
            "id": "deepseek-chat", // 注意：这里是纯ID
            "name": "DeepSeek Chat" // 显示名称可以随意
          }
        ]
      }
    }
  },
   "agents": {
    "defaults": {
      "model": {
        "primary": "deepseek/deepseek-chat"
      },
      "workspace": "/home/zap/.openclaw/workspace"
    }
  }
}
```

- **关键点**：`models` 数组里的 `id` 字段，必须是 `deepseek-chat` 或 `deepseek-reasoner`，不要加 `deepseek/` 这样的前缀

