package com.zclau.sdk;

import com.alibaba.fastjson.JSON;
import com.zclau.sdk.domain.model.ChatCompletionRequest;
import com.zclau.sdk.domain.model.ChatCompletionSyncResponse;
import com.zclau.sdk.domain.model.ModelEnum;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
public class OpenAiCodeReview {

    public static void main(String[] args) throws Exception {
        String aiToken = System.getenv("OPENAI_API_KEY");
        if (aiToken == null || aiToken.trim().isEmpty()) {
            throw new IllegalArgumentException("必须配置 OPENAI_API_KEY 环境参数");
        }

        String codeToken = System.getenv("GITHUB_TOKEN");
        if (null == codeToken || codeToken.isEmpty()) {
            throw new IllegalArgumentException("GITHUB_TOKEN is null");
        }

        try {
            // 1. 代码检出
            String diffCode = getDiffCode();
            // 2. 代码评审
            String reviewLog = codeReview(diffCode, aiToken);
            // 3. 写入评审日志
            String logUrl = writeLog(codeToken, reviewLog);

            log.info("代码评审完成。评审日志: {}, \n 写入:{}", reviewLog, logUrl);
        } catch (Exception e) {
            log.error("代码评审异常，请检查服务状态或参数配置", e);
        }
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

    private static String codeReview(String diffCode, String token) throws Exception {
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

    private static String writeLog(String token, String reviewLog) throws Exception {
        Git git = Git.cloneRepository()
                .setURI("https://github.com/cleverUtd/openai-code-review-log.git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("cleverUtd", token))
                .call();

        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".md";
        File newFile = new File(dateFolder, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(reviewLog);
        }

        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add new file via GitHub Actions").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider("cleverUtd", token)).call();

        return "https://github.com/cleverUtd/openai-code-review-log/blob/main/" + dateFolderName + "/" + fileName;
    }

}