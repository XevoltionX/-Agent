package com.gaocui.llm;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 统一LLM客户端接口
 * 支持DeepSeek和千问两种模型
 */
public interface LLMClient {

    /**
     * 普通对话（非流式）
     * @param messages 历史消息 [{role, content}, ...]
     * @return AI回复文本
     */
    String chat(List<Map<String, String>> messages);

    /**
     * 流式对话（SSE逐字推送）
     * @param messages 历史消息
     * @param onChunk 每收到一个字回调
     * @return 完整回复文本
     */
    String chatStream(List<Map<String, String>> messages, Consumer<String> onChunk);

    /**
     * 图片识别（仅千问VL支持）
     * @param imageUrl 图片URL或base64
     * @param prompt 提示词
     * @return 识别结果（JSON格式: title, description, tags, price）
     */
    String chatWithImage(String imageUrl, String prompt);

    /**
     * 是否支持图片识别
     */
    default boolean supportsVision() { return false; }

    /**
     * 模型名称
     */
    String getModelName();
}
