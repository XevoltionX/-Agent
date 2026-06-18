package com.gaocui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 千问免费临时存储上传
 * 流程：getPolicy → upload to OSS → oss://URL（48h有效）
 */
@Slf4j
@Service
public class QwenUploadService {

    private static final String POLICY_URL = "https://dashscope.aliyuncs.com/api/v1/uploads";
    private final ObjectMapper om = new ObjectMapper();

    private final com.gaocui.config.LLMProperties llmProps;

    public QwenUploadService(com.gaocui.config.LLMProperties llmProps) {
        this.llmProps = llmProps;
    }

    /**
     * 上传文件到千问临时存储
     * @param modelName 模型名 (qwen-vl-plus / qwen3-vl-plus)
     * @param fileBytes 文件内容
     * @param fileName 文件名
     * @return oss:// URL字符串
     */
    @SuppressWarnings("unchecked")
    public String upload(String modelName, byte[] fileBytes, String fileName) throws Exception {
        String apiKey = llmProps.getQwenApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("未配置千问API Key");
        }

        // Step 1: 获取上传凭证
        String getUrl = POLICY_URL + "?action=getPolicy&model=" + URLEncoder.encode(modelName, StandardCharsets.UTF_8);
        HttpURLConnection conn1 = (HttpURLConnection) URI.create(getUrl).toURL().openConnection();
        conn1.setRequestMethod("GET");
        conn1.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn1.setRequestProperty("Content-Type", "application/json");

        Map<String, Object> policyResp = om.readValue(conn1.getInputStream(), Map.class);
        Map<String, Object> data = (Map<String, Object>) policyResp.get("data");

        // Step 2: 上传文件到OSS
        String boundary = "----GaocuiBoundary" + System.currentTimeMillis();
        HttpURLConnection conn2 = (HttpURLConnection) URI.create((String) data.get("upload_host")).toURL().openConnection();
        conn2.setRequestMethod("POST");
        conn2.setDoOutput(true);
        conn2.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        String key = data.get("upload_dir") + "/" + fileName;
        try (OutputStream os = conn2.getOutputStream()) {
            writeField(os, boundary, "OSSAccessKeyId", (String) data.get("oss_access_key_id"));
            writeField(os, boundary, "Signature", (String) data.get("signature"));
            writeField(os, boundary, "policy", (String) data.get("policy"));
            writeField(os, boundary, "x-oss-object-acl", (String) data.get("x_oss_object_acl"));
            writeField(os, boundary, "x-oss-forbid-overwrite", (String) data.get("x_oss_forbid_overwrite"));
            writeField(os, boundary, "key", key);
            writeField(os, boundary, "success_action_status", "200");
            writeFileField(os, boundary, "file", fileName, fileBytes);
            os.write(("--" + boundary + "--\r\n").getBytes());
            os.flush();
        }

        int code = conn2.getResponseCode();
        if (code != 200) {
            String err = new String(conn2.getErrorStream().readAllBytes());
            log.error("千问上传失败: {}", err);
            throw new RuntimeException("文件上传失败: " + code);
        }

        String ossUrl = "oss://" + key;
        log.info("千问上传成功: {} (48h有效)", ossUrl);
        return ossUrl;
    }

    private void writeField(OutputStream os, String boundary, String name, String value) throws IOException {
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        os.write((value + "\r\n").getBytes());
    }

    private void writeFileField(OutputStream os, String boundary, String name, String fileName, byte[] data) throws IOException {
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
        os.write("Content-Type: application/octet-stream\r\n\r\n".getBytes());
        os.write(data);
        os.write("\r\n".getBytes());
    }
}
