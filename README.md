# 简介

这是一个基于 Java 的代码审查自动化工具项目。当提交合并分支的代码后，通过 Github Actions 触发代码评审，并调用 OpenAI 大模型实现遵循阿里 Java 规范的代码质量分析，写入评审日志文件，最后触发消息推送通知用户。
 

设计流程图如下：
![OpenAI 代码自动评审组件流程图](docs/images/OpenAI代码自动评审组件流程图.png)

# 使用方法
### 1. 对接大模型
以 [硅基流动](https://www.siliconflow.cn/) 为例（也可以对接其他模型），先注册好账号。
- 新建 API 密钥: https://cloud.siliconflow.cn/sft-qjv1m1qu9g/account/ak
    - OPENAI_API_TOKEN: sk-o*******************************************
- API 调用地址: https://docs.siliconflow.cn/cn/api-reference/chat-completions/chat-completions
  - OPENAI_API_HOST: https://api.siliconflow.cn/v1/chat/completions
- 选择模型：https://cloud.siliconflow.cn/sft-qjv1m1qu9g/models
  - OPENAI_MODEL: Qwen/Qwen3-14B

### 2. 申请 GitHub 仓库
组件是基于 Github Actions 实现的，所以要提供一个你的 Github 工程库和一个记录评审日志的工程库。

工程库：https://github.com/cleverUtd/openai-code-review - 你创建一个自己的，并提交代码。
日志库：https://github.com/cleverUtd/openai-code-review-log - 你创建一个自己的。

### 3. 申请 GitHub Token
> 地址：https://github.com/settings/tokens
![OpenAI 代码自动评审组件流程图](/docs/images/token.png)
创建后，保存生成的 Token，用于配置到 GitHub Actions 参数中