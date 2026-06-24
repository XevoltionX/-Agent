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
 * 豆包 API 客户端（OpenAI兼容）
 * 支持文本+图片理解(base64)
 */
@Slf4j
@Component
public class DoubaoClient implements LLMClient {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final ObjectMapper om = new ObjectMapper();

    public DoubaoClient(com.gaocui.config.LLMProperties props) {
        this.apiKey = props.getDoubaoApiKey();
        this.baseUrl = props.getDoubaoBaseUrl();
        this.model = props.getDoubaoModel();
    }

    @Override
    public String chat(List<Map<String, String>> messages) {
        StringBuilder sb = new StringBuilder();
        chatStream(messages, sb::append);
        return sb.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String chatStream(List<Map<String, String>> messages, Consumer<String> onChunk) {
        if (apiKey == null || apiKey.isEmpty()) {
            String mock = "（未配置豆包API Key，请设置环境变量 ARK_API_KEY）";
            for (char c : mock.toCharArray()) { onChunk.accept(String.valueOf(c)); }
            return mock;
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("stream", true);

            HttpURLConnection conn = (HttpURLConnection) URI.create(baseUrl + "/chat/completions").toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(600000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(om.writeValueAsBytes(body));
                os.flush();
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                String err = new String(conn.getErrorStream().readAllBytes());
                log.error("豆包 API error {}: {}", code, err);
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
            log.info("豆包 流式回复完成: {}字", fullReply.length());
            return fullReply.toString();

        } catch (Exception e) {
            log.error("豆包 API调用失败", e);
            String fallback = "（连接豆包服务失败）";
            for (char c : fallback.toCharArray()) { onChunk.accept(String.valueOf(c)); }
            return fallback;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String chatWithImage(String imageUrl, String prompt) {
        if (apiKey == null || apiKey.isEmpty()) return "{}";

        try {
            // 千问用oss://URL，豆包用base64
            String mediaType = "image/png";
            if (imageUrl.startsWith("http")) {
                // 远程图片→直接传URL（兜底）
                mediaType = "image/jpeg";
            }

            Map<String, Object> imgPart = new LinkedHashMap<>();
            imgPart.put("type", "image_url");
            Map<String, Object> imgUrlMap = new LinkedHashMap<>();
            imgUrlMap.put("url", imageUrl.startsWith("data:") ? imageUrl : "data:" + mediaType + ";base64," + imageUrl);
            imgPart.put("image_url", imgUrlMap);

            Map<String, Object> textPart = new LinkedHashMap<>();
            textPart.put("type", "text");
            textPart.put("text", prompt);

            Map<String, Object> userMsg = new LinkedHashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", Arrays.asList(imgPart, textPart));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", Collections.singletonList(userMsg));
            body.put("stream", false);
            body.put("max_tokens", 2048);

            HttpURLConnection conn = (HttpURLConnection) URI.create(baseUrl + "/chat/completions").toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(om.writeValueAsBytes(body));
                os.flush();
            }

            String resp = new String(conn.getInputStream().readAllBytes());
            Map<String, Object> data = om.readValue(resp, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) data.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                if (msg != null) {
                    Object msgContent = msg.get("content");
                    if (msgContent instanceof List) {
                        for (Map<String, Object> part : (List<Map<String, Object>>) msgContent) {
                            if (part.containsKey("text")) return (String) part.get("text");
                        }
                    } else if (msgContent instanceof String) {
                        return (String) msgContent;
                    }
                }
            }
            return "{}";
        } catch (Exception e) {
            log.error("豆包VL调用失败", e);
            return "{}";
        }
    }

    @Override
    public boolean supportsVision() { return true; }
    @Override
    public String getModelName() { return model; }
}
