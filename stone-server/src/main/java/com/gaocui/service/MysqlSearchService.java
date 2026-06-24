package com.gaocui.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.entity.Product;
import com.gaocui.entity.ProductImage;
import com.gaocui.entity.ProductTag;
import com.gaocui.enums.ProductStatus;
import com.gaocui.mapper.ProductImageMapper;
import com.gaocui.mapper.ProductMapper;
import com.gaocui.mapper.ProductTagMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MySQL LIKE搜索（云服务器低配版，替代ES）
 */
@Slf4j
@Service
public class MysqlSearchService {

    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;
    private final ProductTagMapper tagMapper;

    public MysqlSearchService(ProductMapper productMapper, ProductImageMapper imageMapper, ProductTagMapper tagMapper) {
        this.productMapper = productMapper;
        this.imageMapper = imageMapper;
        this.tagMapper = tagMapper;
    }

    // 从文本解析预算上限（单位：元）
    // Agent直接返回整数（如100000）
    private java.math.BigDecimal parseBudget(String text) {
        if (text == null || text.isEmpty() || "0".equals(text)) return null;
        try { return new java.math.BigDecimal(text); } catch (Exception e) { return null; }
    }

    public Map<String, Object> search(String keyword, String tagsStr, int page, int size) {
        return search(keyword, tagsStr, page, size, null);
    }

    // 搜索（带预算过滤）
    public Map<String, Object> search(String keyword, String tagsStr, int page, int size, String budgetText) {
        java.math.BigDecimal maxBudget = budgetText != null ? parseBudget(budgetText) : null;
        Map<String, Object> result = new HashMap<>();

        // 查所有已上架商品
        List<Product> allProducts = productMapper.selectList(
            new LambdaQueryWrapper<Product>().eq(Product::getStatus, ProductStatus.PUBLISHED.name())
                .orderByDesc(Product::getPublishedAt)
        );

        List<Product> matched = new ArrayList<>();
        // 拆分关键词：空格/逗号 + 常见非翡翠前缀（预算/数字等）
        String cleanKw = keyword != null ? keyword.replaceAll("\\d+万预算|\\d+左右|预算\\d+|万以内|万左右", "") : "";
        String[] kwWords = (keyword != null && !keyword.isEmpty())
            ? keyword.split("[\\s，,]+") : new String[0];

        for (Product p : allProducts) {
            boolean kwMatch = keyword == null || keyword.isEmpty();
            if (!kwMatch) {
                // 先尝试整体匹配
                if ((p.getTitle() != null && p.getTitle().contains(keyword))
                    || (p.getDescription() != null && p.getDescription().contains(keyword))
                    || hasTag(p.getId(), keyword)) {
                    kwMatch = true;
                }
                // 整体不匹配→用清洗后的关键词
                if (!kwMatch && !cleanKw.equals(keyword) && !cleanKw.isEmpty()) {
                    if ((p.getTitle() != null && p.getTitle().contains(cleanKw))
                        || (p.getDescription() != null && p.getDescription().contains(cleanKw))
                        || hasTag(p.getId(), cleanKw)) {
                        kwMatch = true;
                    }
                }
                // 还不行→拆词逐个匹配
                if (!kwMatch) {
                    String[] words = kwWords.length > 1 ? kwWords : (keyword != null ? keyword.split("") : new String[0]);
                    for (String w : words) {
                        if (w.length() < 2) continue;
                        if ((p.getTitle() != null && p.getTitle().contains(w))
                            || (p.getDescription() != null && p.getDescription().contains(w))
                            || hasTag(p.getId(), w)) {
                            kwMatch = true;
                            break;
                        }
                    }
                }
            }

            // 标签匹配（优先：标签更精准）
            boolean tagMatch = false;
            if (tagsStr != null && !tagsStr.isEmpty()) {
                String[] tags = tagsStr.split(",");
                for (String t : tags) {
                    if (hasTag(p.getId(), t.trim())) { tagMatch = true; break; }
                }
            }

            // keyword或tag满足其一即可
            if (!kwMatch && !tagMatch) continue;

            matched.add(p);
        }

        // 预算软过滤：预算内优先，不足3件用其他补
        if (maxBudget != null) {
            java.math.BigDecimal limit = maxBudget.multiply(new java.math.BigDecimal("1.5"));
            List<Product> inBudget = new ArrayList<>();
            List<Product> outBudget = new ArrayList<>();
            for (Product p : matched) {
                if (p.getPrice() != null && p.getPrice().compareTo(limit) <= 0) inBudget.add(p);
                else outBudget.add(p);
            }
            matched = new ArrayList<>(inBudget);
            int need = size - matched.size();
            if (need > 0 && !outBudget.isEmpty()) {
                matched.addAll(outBudget.subList(0, Math.min(need, outBudget.size())));
            }
        }

        // 分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, matched.size());
        List<Product> paged = matched.subList(Math.min(start, matched.size()), end);

        List<Map<String, Object>> cards = new ArrayList<>();
        for (Product p : paged) {
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("id", p.getId());
            card.put("title", p.getTitle());
            card.put("price", p.getPrice());
            card.put("tags", getTags(p.getId()));
            card.put("images", getImages(p.getId()));
            card.put("merchantId", p.getMerchantId());
            cards.add(card);
        }

        result.put("list", cards);
        result.put("total", matched.size());
        log.info("MySQL搜索: keyword={}, total={}, returned={}", keyword, matched.size(), cards.size());
        return result;
    }

    private boolean hasTag(Long productId, String keyword) {
        List<ProductTag> tags = tagMapper.selectList(
            new LambdaQueryWrapper<ProductTag>().eq(ProductTag::getProductId, productId)
        );
        return tags.stream().anyMatch(t -> t.getTagName().contains(keyword));
    }

    private List<String> getTags(Long productId) {
        return tagMapper.selectList(
            new LambdaQueryWrapper<ProductTag>().eq(ProductTag::getProductId, productId).orderByAsc(ProductTag::getSortOrder)
        ).stream().map(ProductTag::getTagName).collect(Collectors.toList());
    }

    private List<String> getImages(Long productId) {
        return imageMapper.selectList(
            new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, productId).orderByAsc(ProductImage::getSortOrder)
        ).stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
    }
}
