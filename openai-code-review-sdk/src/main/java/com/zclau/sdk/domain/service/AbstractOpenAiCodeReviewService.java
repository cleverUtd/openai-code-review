package com.zclau.sdk.domain.service;

import com.zclau.sdk.infrastructure.git.GitCommand;
import com.zclau.sdk.infrastructure.openai.IOpenAI;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class AbstractOpenAiCodeReviewService implements IOpenAiCodeReviewService {

    protected final GitCommand gitCommand;
    protected final IOpenAI openAI;

    public AbstractOpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
    }

    @Override
    public void exec() {
        try {
            // 1. 获取提交代码
            String diffCode = getDiffCode();
            // 2. 评审代码
            String recommend = codeReview(diffCode);
            // 3. 记录评审结果，返回日志地址
            String logUrl = recordCodeReview(recommend);
            // 4. 发送消息通知： 日志地址、通知内容
            pushMessage(logUrl);
        } catch (Exception e) {
            log.error("openai-code-review error", e);
        }
    }


    protected abstract String getDiffCode() throws IOException, InterruptedException;

    protected abstract String codeReview(String diffCode) throws Exception;

    protected abstract String recordCodeReview(String recommend) throws Exception;

    protected abstract void pushMessage(String log);
}
