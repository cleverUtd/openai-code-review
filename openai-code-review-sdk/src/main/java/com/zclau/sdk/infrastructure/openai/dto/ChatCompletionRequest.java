package com.zclau.sdk.infrastructure.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ChatCompletionRequest {

    private String model;
    private List<Prompt> messages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Prompt {
        private String role;
        private String content;

    }
}
