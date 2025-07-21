package com.zclau.sdk.domain.service.impl;


import com.zclau.sdk.domain.service.AbstractOpenAiCodeReviewService;
import com.zclau.sdk.infrastructure.git.GitCommand;
import com.zclau.sdk.infrastructure.openai.IOpenAI;
import com.zclau.sdk.infrastructure.openai.dto.ChatCompletionRequest;
import com.zclau.sdk.infrastructure.openai.dto.ChatCompletionSyncResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI) {
        super(gitCommand, openAI);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(openAI.getModel());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审，要求遵循阿里Java开发规范。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });

        ChatCompletionSyncResponse completions = openAI.completions(chatCompletionRequest);
        ChatCompletionSyncResponse.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();

    }

    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String log) {

    }
}
