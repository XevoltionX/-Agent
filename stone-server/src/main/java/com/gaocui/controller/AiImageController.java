package com.gaocui.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaocui.common.Result;
import com.gaocui.llm.DoubaoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI图片识别（千问VL）
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiImageController {

    private final DoubaoClient doubaoClient;
    private final com.gaocui.service.ChatService chatService;
    private final ObjectMapper om = new ObjectMapper();

    public AiImageController(com.gaocui.llm.LLMClientFactory factory, com.gaocui.service.ChatService chatService) {
        this.doubaoClient = factory.getDoubao();
        this.chatService = chatService;
    }

    // 图片识别生成商品信息
    @PostMapping("/recognize")
    public Result<?> recognize(@RequestBody Map<String, Object> body) {
        String imageUrl = (String) body.get("imageUrl");
        String basePrompt = (String) body.getOrDefault("prompt",
            "请识别这张翡翠商品图片，用JSON格式返回：\n" +
            "{\n" +
            "  \"title\": \"商品标题（15字内）\",\n" +
            "  \"description\": \"商品简介（25-50字，描述种水/颜色/造型/用途）\",\n" +
            "  \"detail\": \"商品详情（150-300字，包含材质/种水/颜色/尺寸/雕刻工艺/完美度/适用场景）\",\n" +
            "  \"tags\": [\"标签1\", \"标签2\", \"标签3\"],\n" +
            "  \"price\": 预估售价数字\n" +
            "}\n" +
            "若图片100%不是翡翠或多张图片明显不是同一件商品，price返回0。" +
            "只返回JSON，不要其他文字。");

        // 注入已有标签
        String existingTags = chatService.getAllTags();
        String prompt = existingTags.isEmpty() ? basePrompt
            : basePrompt + "\n\n系统已有的商品标签，tags必须从以下选择，不要自创标签：" + existingTags;

        try {
            String result = doubaoClient.chatWithImage(imageUrl, prompt);
            log.info("VL原始回复: {}", result);
            String json = result;
            if (json.contains("```json")) {
                json = json.substring(json.indexOf("```json") + 7, json.lastIndexOf("```"));
            } else if (json.contains("```")) {
                json = json.substring(json.indexOf("```") + 3, json.lastIndexOf("```"));
            }
            json = json.trim();
            try {
                Map<String, Object> data = om.readValue(json, Map.class);
                // 质量检查：标题空或price为0→可能是无关图片
                String title = (String) data.get("title");
                if (title == null || title.trim().isEmpty()) {
                    return Result.fail(400, "识别失败，图片可能非翡翠或非同一物品，请重新上传");
                }
                return Result.ok(data);
            } catch (Exception e2) {
                // 解析失败，返回原始文本
                Map<String, Object> fallback = new java.util.HashMap<>();
                fallback.put("raw", result);
                return Result.ok(fallback);
            }
        } catch (Exception e) {
            log.error("AI识别失败", e);
            return Result.fail(500, "AI识别失败: " + e.getMessage());
        }
    }
}
