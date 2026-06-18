package com.gaocui.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis搜索缓存
 * - 缓存最新搜索结果（2小时）
 * - 统计热门商品点击（2小时）
 */
@Service
public class RedisSearchCache {

    private final StringRedisTemplate redis;
    private final ObjectMapper om = new ObjectMapper();

    private static final String CACHE_KEY = "search:cache:latest";
    private static final String POP_KEY_PREFIX = "search:pop:";
    private static final long TTL_HOURS = 2;

    public RedisSearchCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // 缓存搜索结果
    public void cacheResults(List<Map<String, Object>> cards) {
        if (cards == null || cards.isEmpty()) return;
        try {
            List<Map<String, Object>> slim = new ArrayList<>();
            for (Map<String, Object> c : cards) {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("id", c.get("id"));
                s.put("title", c.get("title"));
                s.put("price", c.get("price"));
                s.put("tags", c.get("tags"));
                Object imgs = c.get("images");
                if (imgs instanceof List && !((List<?>) imgs).isEmpty()) {
                    s.put("images", List.of(((List<?>) imgs).get(0)));
                }
                Object mid = c.getOrDefault("merchantId", c.get("merchant_id"));
                if (mid != null) s.put("merchantId", mid);
                slim.add(s);
            }
            redis.opsForValue().set(CACHE_KEY, om.writeValueAsString(slim), TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception ignored) {}
    }

    // 读取缓存
    public List<Map<String, Object>> getCachedResults() {
        try {
            String json = redis.opsForValue().get(CACHE_KEY);
            if (json == null) return Collections.emptyList();
            return om.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) { return Collections.emptyList(); }
    }

    // 热门商品+1（仅当已在Redis中才计数）
    public void incrementProductHit(List<Map<String, Object>> cards) {
        if (cards == null) return;
        for (Map<String, Object> c : cards) {
            Object id = c.get("id");
            if (id != null) {
                String key = POP_KEY_PREFIX + id;
                if (Boolean.TRUE.equals(redis.hasKey(key))) {
                    redis.opsForValue().increment(key);
                }
            }
        }
    }

    // 获取热门商品ID列表
    public List<Long> getPopularIds(int limit) {
        // 简化实现：扫所有pop key
        Set<String> keys = redis.keys(POP_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return Collections.emptyList();
        List<Map.Entry<Long, Long>> list = new ArrayList<>();
        for (String k : keys) {
            try {
                Long id = Long.parseLong(k.replace(POP_KEY_PREFIX, ""));
                String val = redis.opsForValue().get(k);
                list.add(new AbstractMap.SimpleEntry<>(id, val != null ? Long.parseLong(val) : 0L));
            } catch (Exception ignored) {}
        }
        list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, list.size()); i++) {
            result.add(list.get(i).getKey());
        }
        return result;
    }
}
