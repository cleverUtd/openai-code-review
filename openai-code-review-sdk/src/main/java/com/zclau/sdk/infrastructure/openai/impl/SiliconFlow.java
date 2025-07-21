package com.zclau.sdk.infrastructure.openai.impl;

import com.alibaba.fastjson.JSON;
import com.zclau.sdk.infrastructure.openai.IOpenAI;
import com.zclau.sdk.infrastructure.openai.dto.ChatCompletionRequest;
import com.zclau.sdk.infrastructure.openai.dto.ChatCompletionSyncResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class SiliconFlow implements IOpenAI {

    private final String apiHost;
    private final String apiToken;
    private final String model;

    public SiliconFlow(String apiHost, String apiToken, String model) {
        this.apiHost = apiHost;
        this.apiToken = apiToken;
        this.model = model;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public ChatCompletionSyncResponse completions(ChatCompletionRequest request) throws Exception {
        URL url = new URL(apiHost);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(request).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        return JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
    }
}
