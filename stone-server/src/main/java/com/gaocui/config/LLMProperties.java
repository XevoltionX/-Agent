package com.gaocui.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gaocui.llm")
public class LLMProperties {
    // DeepSeek（默认文本模型）
    private String deepseekApiKey = "";
    private String deepseekBaseUrl = "https://api.deepseek.com";
    private String deepseekProModel = "deepseek-v4-pro";
    private String deepseekFlashModel = "deepseek-v4-flash";

    // 千问（图片识别+超时备选）
    private String qwenApiKey = "";
    private String qwenBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";  // 不含尾/chat/completions
    private String qwenModel = "qwen-plus";
    private String qwenVisionModel = "qwen-vl-plus";
}
