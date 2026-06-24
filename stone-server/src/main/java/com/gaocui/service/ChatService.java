package com.gaocui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaocui.dto.ChatRequestDTO;
import com.gaocui.entity.ChatHistory;
import com.gaocui.mapper.ChatHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ChatService {

    private final ProductSearchService searchService;   // ES（可能为null，当mysql模式）
    private final MysqlSearchService mysqlSearch;        // MySQL
    private final ChatHistoryMapper historyMapper;
    @org.springframework.beans.factory.annotation.Value("${gaocui.search.mode:mysql}")
    private String searchMode;
    private final com.gaocui.mapper.JadeDemandMapper demandMapper;
    private final com.gaocui.mapper.ProductTagMapper tagMapper;
    private final RedisSearchCache cacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ObjectProvider<ProductSearchService> searchServiceProvider, ChatHistoryMapper historyMapper,
                       com.gaocui.mapper.JadeDemandMapper demandMapper, com.gaocui.mapper.ProductTagMapper tagMapper,
                       RedisSearchCache cacheService, MysqlSearchService mysqlSearch) {
        this.searchService = searchServiceProvider.getIfAvailable();  // mysql模式时为null
        this.mysqlSearch = mysqlSearch;
        this.historyMapper = historyMapper;
        this.demandMapper = demandMapper;
        this.tagMapper = tagMapper;
        this.cacheService = cacheService;
    }

    public Map<String, Object> chat(ChatRequestDTO dto) {
        String message = dto.getMessage();
        String sessionId = dto.getSessionId() != null ? dto.getSessionId() : UUID.randomUUID().toString();
        Map<String, Object> result = new HashMap<>();

        // Agent预留字段
        result.put("sessionId", sessionId);
        result.put("isJadeQuery", true);
        result.put("intent", "DIRECT_SEARCH");
        result.put("extractedParams", new HashMap<>());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        result.put("timestamp", timestamp);

        // 保存用户消息（只存文本）
        saveHistory(sessionId, "user", message);

        // 搜索
        log.info("ChatService收到消息: {}", message);
        Map<String, Object> searchResult = searchWithTags(message, null);
        log.info("搜索结果: total={}", searchResult != null ? searchResult.get("total") : "null");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> matched = searchResult != null ? (List<Map<String, Object>>) searchResult.get("list") : Collections.emptyList();

        String reply;
        if (matched != null && !matched.isEmpty()) {
            reply = "根据您的需求，我为您匹配到以下货源：";
            result.put("cards", matched);
        } else {
            reply = "暂时没有匹配的货源，请尝试调整您的需求条件~";
            result.put("cards", Collections.emptyList());
        }
        result.put("reply", reply);

        // 保存AI回复（只存文本，不存cards——base64图片太大了）
        saveHistory(sessionId, "ai", reply);

        return result;
    }

    private void saveHistory(String sessionId, String role, String content) {
        try {
            ChatHistory h = new ChatHistory();
            h.setSessionId(sessionId);
            h.setRole(role);
            h.setContent(content);
            h.setCards(null);
            historyMapper.insert(h);
        } catch (Exception e) {
            log.warn("保存对话历史失败: {}", e.getMessage());
        }
    }

    // 加载历史对话（多轮上下文）
    public java.util.List<com.gaocui.entity.ChatHistory> getHistory(String sessionId, int limit) {
        try {
            return historyMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.gaocui.entity.ChatHistory>()
                    .eq(com.gaocui.entity.ChatHistory::getSessionId, sessionId)
                    .orderByAsc(com.gaocui.entity.ChatHistory::getId)
                    .last("LIMIT " + limit)
            );
        } catch (Exception e) { return java.util.Collections.emptyList(); }
    }

    // LLM对话保存 + ES搜索 的辅助方法
    public void saveChat(String sessionId, String userMsg, String aiReply) {
        saveHistory(sessionId, "user", userMsg);
        saveHistory(sessionId, "ai", aiReply);
    }

    public Map<String, Object> searchOnly(String keyword) {
        if ("elasticsearch".equals(searchMode) && searchService != null) return searchService.search(keyword, null, 1, 3);
        return mysqlSearch.search(keyword, null, 1, 3, keyword);
    }

    public Map<String, Object> searchWithTags(String keyword, String tags) {
        if ("elasticsearch".equals(searchMode) && searchService != null) return searchService.search(keyword, tags, 1, 3);
        return mysqlSearch.search(keyword, tags, 1, 3, keyword);
    }

    // 带预算的搜索（budgetText=用户原始输入）
    public Map<String, Object> searchWithBudget(String keyword, String tags, String budgetText) {
        if ("elasticsearch".equals(searchMode) && searchService != null) return searchService.search(keyword, tags, 1, 3);
        return mysqlSearch.search(keyword, tags, 1, 3, budgetText);
    }

    // 保存Agent需求分析
    public void saveDemand(String sessionId, String userMessage, Map<String, Object> agentData) {
        try {
            com.gaocui.entity.JadeDemand d = new com.gaocui.entity.JadeDemand();
            d.setSessionId(sessionId);
            d.setUserMessage(userMessage);
            d.setDemandTitle((String) agentData.get("demandTitle"));
            d.setDemandDescription((String) agentData.get("demandDescription"));
            d.setDemandDetail((String) agentData.get("demandDetail"));
            d.setEsKeyword((String) agentData.get("keyword"));
            d.setEsTags((String) agentData.get("tags"));
            Object tagsList = agentData.get("tagsList");
            d.setTags(tagsList != null ? objectMapper.writeValueAsString(tagsList) : "[]");
            Object specs = agentData.get("specs");
            d.setSpecs(specs != null ? objectMapper.writeValueAsString(specs) : "{}");
            d.setMatchCount(0);
            demandMapper.insert(d);
        } catch (Exception e) {
            log.warn("保存需求分析失败: {}", e.getMessage());
        }
    }

    // 判断上一轮对话是否翡翠搜索（控制多轮上下文长度）
    public boolean wasLastRoundJade(String sessionId) {
        try {
            Long count = demandMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.gaocui.entity.JadeDemand>()
                    .eq(com.gaocui.entity.JadeDemand::getSessionId, sessionId)
            );
            return count != null && count > 0;
        } catch (Exception e) { return false; }
    }

    // Redis缓存
    public java.util.List<Map<String, Object>> getCachedCards() { return cacheService.getCachedResults(); }
    public void cacheSearchResults(java.util.List<Map<String, Object>> cards) {
        if (cards != null && !cards.isEmpty()) { cacheService.cacheResults(cards); cacheService.incrementProductHit(cards); }
    }

    // 获取前100高频标签（供Agent/VL参考）
    public String getAllTags() {
        try {
            java.util.List<java.util.Map<String, Object>> topTags = tagMapper.getTopTags(100);
            if (topTags == null || topTags.isEmpty()) return "";
            return topTags.stream()
                .map(m -> (String) m.get("tag_name"))
                .collect(java.util.stream.Collectors.joining("、"));
        } catch (Exception e) { return ""; }
    }

    public String slimCardsToJson(java.util.List<Map<String, Object>> cards) {
        java.util.List<Map<String, Object>> slim = new java.util.ArrayList<>();
        for (Map<String, Object> c : cards) {
            Map<String, Object> s = new java.util.LinkedHashMap<>();
            s.put("id", c.get("id"));
            s.put("title", c.get("title"));
            s.put("price", c.get("price"));
            s.put("tags", c.get("tags"));
            s.put("merchantId", c.getOrDefault("merchantId", c.get("merchant_id")));
            Object imgs = c.get("images");
            if (imgs instanceof java.util.List && !((java.util.List<?>) imgs).isEmpty()) {
                s.put("images", java.util.Collections.singletonList(((java.util.List<?>) imgs).get(0)));
            }
            slim.add(s);
        }
        try { return objectMapper.writeValueAsString(slim); } catch (Exception e) { return "[]"; }
    }
}
