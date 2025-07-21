package com.zclau.sdk;

import com.zclau.sdk.domain.service.IOpenAiCodeReviewService;
import com.zclau.sdk.domain.service.impl.OpenAiCodeReviewService;
import com.zclau.sdk.infrastructure.git.GitCommand;
import com.zclau.sdk.infrastructure.openai.IOpenAI;
import com.zclau.sdk.infrastructure.openai.impl.SiliconFlow;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenAiCodeReview {

    public static void main(String[] args) throws Exception {
        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_LOG_URI"),
                getEnv("GITHUB_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );

        IOpenAI openAI = new SiliconFlow(getEnv("OPENAI_API_HOST"), getEnv("OPENAI_API_KEY"), getEnv("OPENAI_MODEL"));

        IOpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitCommand, openAI);
        openAiCodeReviewService.exec();

        log.info("openai-code-review done!");
    }


    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(key + " is blank");
        }
        return value;
    }
}