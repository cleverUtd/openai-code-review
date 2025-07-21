package com.zclau.sdk.infrastructure.openai;


import com.zclau.sdk.infrastructure.openai.dto.ChatCompletionRequest;
import com.zclau.sdk.infrastructure.openai.dto.ChatCompletionSyncResponse;

public interface IOpenAI {

    String getModel();
    ChatCompletionSyncResponse completions(ChatCompletionRequest request) throws Exception;
}
