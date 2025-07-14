package com.zclau.sdk;

import com.alibaba.fastjson.JSON;
import com.zclau.sdk.domain.model.ChatCompletionRequest;
import com.zclau.sdk.domain.model.ChatCompletionSyncResponse;
import com.zclau.sdk.domain.model.ModelEnum;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class OpenAiCodeReview {

    private static final String token = "sk-tdclxfeowfvmmwmbxsmrrgdzoznmdnzoftneuwintywvopni";

    public static void main(String[] args) throws Exception {
        System.out.println("测试执行");
        // 1. 代码检出
        String diffCode = getDiffCode();
        // 2. 代码评审
        String reviewLog = codeReview(diffCode);
        System.out.println("code review:" + reviewLog);
    }


    private static String getDiffCode() throws Exception {

        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code:" + exitCode);
        System.out.println("diff code：" + diffCode);
        return diffCode.toString();
    }

    private static String codeReview(String diffCode) throws Exception {
        URL url = new URL("https://api.siliconflow.cn/v1/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        ChatCompletionRequest chatCompletionRequest = getChatCompletionRequest(diffCode);
        System.out.println("请求：" + JSON.toJSONString(chatCompletionRequest));

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        System.out.println("评审结果：" + content);

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "评审结果为空";
        }
        return response.getChoices().get(0).getMessage().getContent();

    }

    private static ChatCompletionRequest getChatCompletionRequest(String diffCode) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(ModelEnum.Qwen3_32B.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审，要求遵循阿里Java开发规范。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });
        return chatCompletionRequest;
    }
}