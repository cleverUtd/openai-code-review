# 简介

这是一个基于 Java 的代码审查自动化工具项目。当提交合并分支的代码后，通过 Github Actions 触发代码评审，并调用 OpenAI
大模型实现遵循阿里 Java 规范的代码质量分析，写入评审日志文件，最后触发消息推送通知用户。

设计流程图如下：
![OpenAI代码自动评审组件流程图](/docs/images/OpenAI-codeReview-flow.png)
https://www.edrawmax.cn/online/share.html?code=ff53075a679211f0a688b54c6d3c7722

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

![GitHub Token](/docs/images/token.png)

创建后，保存生成的 Token，用于配置到 GitHub Actions 参数中

### 4. GitHub Actions 配置

##### 4.1 配置参数

地址：https://github.com/cleverUtd/xxxxxx/settings - 换成你的项目工程，进入到 Setting -> Secrets and variables ->
Actions -> Repository secrets -> New repository secret

![参数配置](/docs/images/variables.png)

| Name                | Secret                                                     |
|---------------------|------------------------------------------------------------|
| OPENAI_API_HOST     | https://api.siliconflow.cn/v1/chat/completions             |
| OPENAI_API_TOKEN    | 39580e34e175019c230fdd519817b381.F*****pzqiRDcAk - 使用你的    |
| OPENAI_MODEL        | Qwen/Qwen3-14B  使用你的                                       |
| CODE_TOKEN          | ghp_KWBsnzwoQR4OXO4o3XjIJjVU****GsS1 - 使用你的                |
| CODE_REVIEW_LOG_URI | https://github.com/cleverUtd/openai-code-review-log - 使用你的 |


##### 4.2 配置脚本
![.github/workflows](/docs/images/workflows.png)

- 项目根目录下创建 `.github/workflows` 文件夹，在该文件夹下创建 `xxxx.yml` 文件，内容如下：
````
name: Build and Run OpenAiCodeReview By Remote Jar

on:
  push:
    branches:
      - main
jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v2
        with:
          fetch-depth: 2

      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          distribution: 'temurin'  # 你可以选择其他发行版，如 'adopt' 或 'zulu'
          java-version: '11'

      - name: Create libs directory
        run: mkdir -p ./libs

      - name: Download openai-code-review-sdk JAR
        run: wget -O ./libs/openai-code-review-sdk-1.0.jar https://github.com/cleverUtd/openai-code-review/releases/download/v1.0/openai-code-review-sdk-1.0.jar

      - name: Get repository name
        id: repo-name
        run: echo "REPO_NAME=${GITHUB_REPOSITORY##*/}" >> $GITHUB_ENV

      - name: Get branch name
        id: branch-name
        run: echo "BRANCH_NAME=${GITHUB_REF#refs/heads/}" >> $GITHUB_ENV

      - name: Get commit author
        id: commit-author
        run: echo "COMMIT_AUTHOR=$(git log -1 --pretty=format:'%an <%ae>')" >> $GITHUB_ENV

      - name: Get commit message
        id: commit-message
        run: echo "COMMIT_MESSAGE=$(git log -1 --pretty=format:'%s')" >> $GITHUB_ENV

      - name: Print repository, branch name, commit author, and commit message
        run: |
          echo "Repository name is ${{ env.REPO_NAME }}"
          echo "Branch name is ${{ env.BRANCH_NAME }}"
          echo "Commit author is ${{ env.COMMIT_AUTHOR }}"
          echo "Commit message is ${{ env.COMMIT_MESSAGE }}"      

      - name: Run Code Review
        run: java -jar ./libs/openai-code-review-sdk-1.0.jar
        env:
          # Github 配置
          GITHUB_REVIEW_LOG_URI: ${{ secrets.CODE_REVIEW_LOG_URI }}
          GITHUB_TOKEN: ${{ secrets.CODE_TOKEN }}
          COMMIT_PROJECT: ${{ env.REPO_NAME }}
          COMMIT_BRANCH: ${{ env.BRANCH_NAME }}
          COMMIT_AUTHOR: ${{ env.COMMIT_AUTHOR }}
          COMMIT_MESSAGE: ${{ env.COMMIT_MESSAGE }}

          # OpenAI 配置
          OPENAI_API_KEY: ${{ secrets.OPENAI_API_TOKEN }}
          OPENAI_API_HOST: ${{ secrets.OPENAI_API_HOST }}
          OPENAI_MODEL: ${{ secrets.OPENAI_MODEL }}
````
- 接下来你提交代码就会自动触发代码评审啦
