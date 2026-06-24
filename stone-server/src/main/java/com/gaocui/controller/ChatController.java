package com.gaocui.controller;

import com.gaocui.common.Result;
import com.gaocui.dto.ChatRequestDTO;
import com.gaocui.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final com.gaocui.llm.LLMClientFactory llmFactory;

    public ChatController(ChatService chatService, com.gaocui.llm.LLMClientFactory llmFactory) {
        this.chatService = chatService;
        this.llmFactory = llmFactory;
    }

    @PostMapping("/chat")
    public Result<?> chat(@RequestBody ChatRequestDTO dto) {
        return Result.ok(chatService.chat(dto));
    }

    // SSE流式对话（LLM + ES搜索）
    @GetMapping("/chat/stream")
    public SseEmitter chatStream(@RequestParam String message,
                                  @RequestParam(required = false) String sessionId,
                                  @RequestParam(required = false, defaultValue = "pro") String model,
                                  @RequestParam(required = false, defaultValue = "deepseek") String provider,
                                  @RequestParam(required = false, defaultValue = "0") int timeout) {
        SseEmitter emitter = new SseEmitter(660000L);  // 11分钟

        CompletableFuture.runAsync(() -> {
            try {
                String sid = sessionId != null ? sessionId : UUID.randomUUID().toString();
                emitter.send(SseEmitter.event().name("status").data("thinking"));

                // Agent系统提示词（注入已有标签）
                String existingTags = chatService.getAllTags();
                String tagHint = existingTags.isEmpty() ? "" : "\n\n系统已有的商品标签（必须从以下标签中选择，不要自创标签）：" + existingTags;
                String agentPrompt = "你是高翠AI，翡翠选购顾问。根据用户输入生成JSON需求数据供后端搜索商品。\n\n" +
                    "## 核心原则\n" +
                    "- 用户输入明确包含翡翠相关关键词（翡翠、玉、手镯、吊坠、戒面、平安扣、种水、圈口、A货等）时，输出 isJadeQuery: true。\n" +
                    "- 用户输入可以合理联想翡翠时（如\"送礼\"→联想翡翠送礼），仍输出 true。\n" +
                    "- 用户输入完全与翡翠无关且无法联想时（如纯技术问题\"怎么写代码\"、纯闲聊\"讲个笑话\"），允许输出 isJadeQuery: false。\n" +
                    "- 当不确定时，优先走 true，尝试联想翡翠场景。\n\n" +
                    "## 上下文判断\n" +
                    "- 如果历史对话显示上轮已完成商品搜索（返回了卡片），而用户本轮说的是全新需求（不同品类/不同场景），应视为新搜索，keyword/tags重新提取，不要参考上轮。\n" +
                    "- 如果用户本轮在询问上轮商品的细节、价格、款式变化，应视为跟进，keyword/tags基于上轮微调。\n" +
                    "- 如果用户用简短词语（如\"手镯\"、\"平安扣\"），结合历史判断是全新搜索还是对上轮的补充。\n\n" +
                    "## 多语言\n" +
                    "- keyword、tags、demandTitle等后端使用的字段，始终使用中文。\n" +
                    "- reply 字段使用用户的语言回复。用户用英文提问则 reply 用英文，用户用日文则 reply 用日文。\n\n" +
                    "## JSON格式\n" +
                    "只输出纯JSON，不添加Markdown标记。\n\n" +
                    "true:{\"isJadeQuery\":true,\"reply\":\"回复\",\"keyword\":\"中文搜索词\",\"tags\":\"标签1,标签2\",\"budget\":预算整数(人民币元,如100000,无则0),\"demandTitle\":\"15字内\",\"demandDescription\":\"50字内\",\"demandDetail\":\"300字内\",\"tagsList\":[\"标签1\"],\"specs\":{\"产品品类\":\"\",\"核心卖点\":\"\",\"主石材质\":\"\",\"翡翠造型\":\"\",\"翡翠种水\":\"\",\"翡翠颜色\":\"\",\"配石材质\":\"\",\"镶嵌工艺\":\"\",\"瑕疵情况\":\"\",\"款式风格\":\"\",\"适用场景\":\"\",\"产品寓意\":\"\",\"镶嵌材质\":\"\",\"镶嵌配件\":\"\",\"尺寸规格\":\"\"}}\n" +
                    "false:{\"isJadeQuery\":false,\"reply\":\"引导语(用用户语言)\",\"keyword\":\"\",\"tags\":\"\",\"demandTitle\":\"\",\"demandDescription\":\"\",\"demandDetail\":\"\",\"tagsList\":[],\"specs\":{}}" + tagHint;

                // 判断上一轮是否翡翠搜索 → 决定加载多少历史
                boolean lastRoundWasJade = chatService.wasLastRoundJade(sid);
                int historyLimit = lastRoundWasJade ? 2 : 6;  // true→1轮(2条) false→3轮(6条)

                java.util.List<Map<String, String>> llmMessages = new java.util.ArrayList<>();
                llmMessages.add(Map.of("role", "system", "content", agentPrompt));

                // 加载历史对话
                java.util.List<com.gaocui.entity.ChatHistory> history = chatService.getHistory(sid, historyLimit);
                if (history != null) {
                    for (com.gaocui.entity.ChatHistory h : history) {
                        // DeepSeek需要"assistant"而非"ai"
                        String role = "ai".equals(h.getRole()) ? "assistant" : h.getRole();
                        llmMessages.add(Map.of("role", role, "content", h.getContent()));
                    }
                }

                // XSS过滤
                String safeMessage = message.replaceAll("[<>\"']", "");
                llmMessages.add(Map.of("role", "user", "content", safeMessage));

                // 豆包：收集完整JSON，解析后再流式发reply
                StringBuilder buffer = new StringBuilder();
                java.util.function.Consumer<String> onChunk = chunk -> buffer.append(chunk);

                com.gaocui.llm.DoubaoClient doubao = llmFactory.getDoubao();
                String llmReply = doubao.chatStream(llmMessages, onChunk);

                // 解析Agent JSON，只发reply给前端
                try {
                    String json = buffer.toString().trim();
                    // 去掉可能的markdown包裹
                    if (json.startsWith("```")) json = json.substring(json.indexOf("\n") + 1);
                    if (json.endsWith("```")) json = json.substring(0, json.lastIndexOf("```")).trim();

                    Map<String, Object> agentData = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
                    // Agent可能返回字符串"true"而非布尔值
                    Object isJadeRaw = agentData.get("isJadeQuery");
                    boolean isJade = isJadeRaw instanceof Boolean ? (Boolean) isJadeRaw : "true".equals(String.valueOf(isJadeRaw));
                    String agentReply = (String) agentData.get("reply");
                    String keyword = (String) agentData.get("keyword");
                    String tags = (String) agentData.get("tags");
                    log.info("Agent解析: isJade={}, keyword={}, tags={}", isJade, keyword, tags);

                    // 流式发送reply
                    if (agentReply != null && !agentReply.isEmpty()) {
                        for (char c : agentReply.toCharArray()) {
                            emitter.send(SseEmitter.event().name("chunk").data(String.valueOf(c)));
                            Thread.sleep(25);
                        }
                    }

                    // 搜索（Agent解析的keyword+tags，预算从Agent JSON获取）
                    String searchKeyword = (keyword != null && !keyword.isEmpty()) ? keyword : safeMessage;
                    String budgetStr = String.valueOf(agentData.getOrDefault("budget", ""));
                    Map<String, Object> searchResult = chatService.searchWithBudget(searchKeyword, tags, budgetStr);
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String, Object>> cards = searchResult != null ?
                        (java.util.List<Map<String, Object>>) searchResult.get("list") : Collections.emptyList();

                    if (cards != null && !cards.isEmpty()) {
                        emitter.send(SseEmitter.event().name("cards").data(chatService.slimCardsToJson(cards)));
                        chatService.cacheSearchResults(cards);  // 缓存到Redis
                    }

                    // 存jade_demands
                    chatService.saveDemand(sid, message, agentData);

                    // 保存对话
                    chatService.saveChat(sid, message, agentReply != null ? agentReply : llmReply);

                } catch (Exception parseEx) {
                    log.warn("Agent JSON解析失败: {}", parseEx.getMessage());
                    chatService.saveChat(sid, message, llmReply);
                }

                emitter.send(SseEmitter.event().name("done").data(sid));
                emitter.complete();

            } catch (Exception e) {
                log.error("SSE异常", e);
                try { emitter.send(SseEmitter.event().name("error").data("服务异常")); } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
