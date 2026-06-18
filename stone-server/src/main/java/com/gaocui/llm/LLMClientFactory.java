package com.gaocui.llm;

import com.gaocui.config.LLMProperties;
import org.springframework.stereotype.Component;

/**
 * LLM客户端工厂
 * - 文字生成：默认DeepSeek，超时降级千问
 * - 图片识别：固定千问VL
 */
@Component
public class LLMClientFactory {

    private final DeepSeekClient deepSeekClient;
    private final QwenClient qwenClient;

    public LLMClientFactory(DeepSeekClient deepSeekClient, QwenClient qwenClient) {
        this.deepSeekClient = deepSeekClient;
        this.qwenClient = qwenClient;
    }

    /** 默认文字模型：DeepSeek */
    public LLMClient getTextClient() {
        return deepSeekClient;
    }

    /** 图片识别：千问VL */
    public LLMClient getVisionClient() {
        return qwenClient;
    }

    /** DeepSeek超时时降级：千问 */
    public LLMClient getFallbackClient() {
        return qwenClient;
    }

    public DeepSeekClient getDeepSeek() { return deepSeekClient; }
    public QwenClient getQwen() { return qwenClient; }
}
