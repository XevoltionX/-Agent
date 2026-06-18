package com.gaocui.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

/**
 * DeepSeek API 客户端（OpenAI兼容）
 */
@Slf4j
@Component
public class DeepSeekClient implements LLMClient {

    private final String apiKey;
    private final String baseUrl;
    private final String proModel;
    private final String flashModel;
    private final ObjectMapper om = new ObjectMapper();

    public DeepSeekClient(com.gaocui.config.LLMProperties props) {
        this.apiKey = props.getDeepseekApiKey();
        this.baseUrl = props.getDeepseekBaseUrl();
        this.proModel = props.getDeepseekProModel();
        this.flashModel = props.getDeepseekFlashModel();
    }

    @Override
    public String chat(List<Map<String, String>> messages) {
        StringBuilder sb = new StringBuilder();
        chatStreamWithModel(messages, sb::append, proModel);
        return sb.toString();
    }

    @Override
    public String chatStream(List<Map<String, String>> messages, Consumer<String> onChunk) {
        return chatStreamWithModel(messages, onChunk, proModel);
    }

    /** 使用Flash模型（更快更便宜） */
    public String chatStreamFlash(List<Map<String, String>> messages, Consumer<String> onChunk) {
        return chatStreamWithModel(messages, onChunk, flashModel);
    }

    /** 使用Pro模型（更强推理） */
    public String chatStreamPro(List<Map<String, String>> messages, Consumer<String> onChunk) {
        return chatStreamWithModel(messages, onChunk, proModel);
    }

    @SuppressWarnings("unchecked")
    private String chatStreamWithModel(List<Map<String, String>> messages, Consumer<String> onChunk, String modelName) {
        if (apiKey == null || apiKey.isEmpty()) {
            String mock = "（未配置DeepSeek API Key，请设置环境变量 DEEPSEEK_API_KEY）";
            for (char c : mock.toCharArray()) { onChunk.accept(String.valueOf(c)); }
            return mock;
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", modelName);
            body.put("messages", messages);
            body.put("stream", true);
            body.put("thinking", Map.of("type", "enabled"));
            body.put("reasoning_effort", "medium");
            body.put("max_tokens", 4096);

            HttpURLConnection conn = (HttpURLConnection) URI.create(baseUrl + "/chat/completions").toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(600000);  // 10分钟

            try (OutputStream os = conn.getOutputStream()) {
                os.write(om.writeValueAsBytes(body));
                os.flush();
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                String err = new String(conn.getErrorStream().readAllBytes());
                log.error("DeepSeek API error {}: {}", code, err);
                String fallback = "（AI服务暂时不可用）";
                for (char c : fallback.toCharArray()) { onChunk.accept(String.valueOf(c)); }
                return fallback;
            }

            StringBuilder fullReply = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                        String json = line.substring(6);
                        try {
                            Map<String, Object> data = om.readValue(json, Map.class);
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) data.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                if (delta != null) {
                                    // 只取实际回复content，跳过reasoning_content（思考过程）
                                    Object content = delta.get("content");
                                    if (content != null && !content.toString().isEmpty()) {
                                        String chunk = content.toString();
                                        fullReply.append(chunk);
                                        onChunk.accept(chunk);
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
            log.info("DeepSeek 流式回复完成: {}字", fullReply.length());
            return fullReply.toString();

        } catch (Exception e) {
            log.error("DeepSeek API调用失败", e);
            String fallback = "（连接AI服务失败：" + e.getMessage() + "）";
            for (char c : fallback.toCharArray()) { onChunk.accept(String.valueOf(c)); }
            return fallback;
        }
    }

    @Override
    public String chatWithImage(String imageUrl, String prompt) {
        throw new UnsupportedOperationException("DeepSeek不支持图片识别");
    }

    @Override
    public String getModelName() { return proModel; }
    public String getProModelName() { return proModel; }
    public String getFlashModelName() { return flashModel; }
}
