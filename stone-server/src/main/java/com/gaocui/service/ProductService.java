package com.gaocui.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.common.BizException;
import com.gaocui.dto.PublishProductDTO;
import com.gaocui.entity.*;
import com.gaocui.enums.ProductStatus;
import com.gaocui.mapper.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;
    private final ProductTagMapper tagMapper;
    private final ProductSearchService searchService;  // mysql模式时为null

    public ProductService(ProductMapper productMapper, ProductImageMapper imageMapper,
                          ProductTagMapper tagMapper, ObjectProvider<ProductSearchService> searchServiceProvider) {
        this.productMapper = productMapper;
        this.imageMapper = imageMapper;
        this.tagMapper = tagMapper;
        this.searchService = searchServiceProvider.getIfAvailable();
    }

    private static final int VIP_MAX = 100;
    private static final int FREE_MAX = 2;

    // 构建商品详情Map（含图片和标签）
    private Map<String, Object> buildDetail(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("merchantId", p.getMerchantId());
        map.put("merchant_id", p.getMerchantId());
        map.put("title", p.getTitle());
        map.put("description", p.getDescription());
        map.put("detail", p.getDetail());
        map.put("price", p.getPrice());
        map.put("status", p.getStatus());
        map.put("published_at", p.getPublishedAt() != null ? p.getPublishedAt().toString().replace("T", " ").substring(0, 16) : null);
        map.put("publishedAt", p.getPublishedAt());
        map.put("created_at", p.getCreatedAt() != null ? p.getCreatedAt().toString().replace("T", " ").substring(0, 16) : "");
        map.put("createdAt", p.getCreatedAt());
        // 关联图片
        List<ProductImage> images = imageMapper.selectList(
            new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, p.getId()).orderByAsc(ProductImage::getSortOrder)
        );
        map.put("images", images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList()));
        // 关联标签
        List<ProductTag> tags = tagMapper.selectList(
            new LambdaQueryWrapper<ProductTag>().eq(ProductTag::getProductId, p.getId()).orderByAsc(ProductTag::getSortOrder)
        );
        map.put("tags", tags.stream().map(ProductTag::getTagName).collect(Collectors.toList()));
        return map;
    }

    @Transactional
    public Map<String, Object> publish(Long merchantId, boolean isVip, PublishProductDTO dto) {
        // 校验发布限额
        Long publishedCount = productMapper.selectCount(
            new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId).eq(Product::getStatus, ProductStatus.PUBLISHED.name())
        );
        int max = isVip ? VIP_MAX : FREE_MAX;
        if (publishedCount >= max) {
            throw new BizException(403, isVip ? "请下架部分商品后再发布" : "需升级VIP提升发布额度");
        }

        Product p = new Product();
        p.setMerchantId(merchantId);
        p.setTitle(dto.getTitle());
        p.setDescription(dto.getDescription());
        p.setDetail(dto.getDetail());
        p.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        String status = dto.getStatus() != null ? dto.getStatus().toUpperCase() : ProductStatus.PUBLISHED.name();
        p.setStatus(status);
        if (ProductStatus.PUBLISHED.name().equals(status)) {
            p.setPublishedAt(LocalDateTime.now());
        }
        productMapper.insert(p);

        // 保存图片
        if (dto.getImages() != null) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                ProductImage img = new ProductImage();
                img.setProductId(p.getId());
                img.setImageUrl(dto.getImages().get(i));
                img.setSortOrder(i);
                imageMapper.insert(img);
            }
        }

        // 保存标签
        if (dto.getTags() != null) {
            for (int i = 0; i < dto.getTags().size(); i++) {
                ProductTag tag = new ProductTag();
                tag.setProductId(p.getId());
                tag.setTagName(dto.getTags().get(i));
                tag.setSortOrder(i);
                tagMapper.insert(tag);
            }
        }

        // 同步ES
        syncToEs(p);
        return buildDetail(p);
    }

    @Transactional
    public Map<String, Object> update(Long id, PublishProductDTO dto) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new BizException("商品不存在");

        p.setTitle(dto.getTitle());
        p.setDescription(dto.getDescription());
        p.setDetail(dto.getDetail());
        if (dto.getPrice() != null) p.setPrice(dto.getPrice());
        productMapper.updateById(p);

        // 替换图片
        imageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, id));
        if (dto.getImages() != null) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                ProductImage img = new ProductImage();
                img.setProductId(id);
                img.setImageUrl(dto.getImages().get(i));
                img.setSortOrder(i);
                imageMapper.insert(img);
            }
        }

        // 替换标签
        tagMapper.delete(new LambdaQueryWrapper<ProductTag>().eq(ProductTag::getProductId, id));
        if (dto.getTags() != null) {
            for (int i = 0; i < dto.getTags().size(); i++) {
                ProductTag tag = new ProductTag();
                tag.setProductId(id);
                tag.setTagName(dto.getTags().get(i));
                tag.setSortOrder(i);
                tagMapper.insert(tag);
            }
        }

        syncToEs(p);
        return buildDetail(p);
    }

    public void delete(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new BizException("商品不存在");
        productMapper.deleteById(id);
        imageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, id));
        tagMapper.delete(new LambdaQueryWrapper<ProductTag>().eq(ProductTag::getProductId, id));
        if (searchService != null) { try { searchService.deleteProduct(id); } catch (Exception ignored) {} }
    }

    public void updateStatus(Long id, String status) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new BizException("商品不存在");
        p.setStatus(status.toUpperCase());
        if ("PUBLISHED".equalsIgnoreCase(status)) {
            p.setPublishedAt(LocalDateTime.now());
        }
        productMapper.updateById(p);
        syncToEs(p);
    }

    private void syncToEs(Product p) {
        if (searchService == null) return;
        try {
            if (ProductStatus.PUBLISHED.name().equals(p.getStatus())) {
                List<String> images = imageMapper.selectList(
                    new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, p.getId()).orderByAsc(ProductImage::getSortOrder)
                ).stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
                List<String> tags = tagMapper.selectList(
                    new LambdaQueryWrapper<ProductTag>().eq(ProductTag::getProductId, p.getId()).orderByAsc(ProductTag::getSortOrder)
                ).stream().map(ProductTag::getTagName).collect(Collectors.toList());
                searchService.indexProduct(p, images, tags);
            } else {
                searchService.deleteProduct(p.getId());
            }
        } catch (Exception ignored) {}
    }

    public Map<String, Object> getMerchantProducts(Long merchantId, String status) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
            .eq(Product::getMerchantId, merchantId)
            .orderByDesc(Product::getCreatedAt);
        if (status != null && !status.isEmpty()) {
            qw.eq(Product::getStatus, status.toUpperCase());
        }
        List<Product> list = productMapper.selectList(qw);
        List<Map<String, Object>> result = list.stream().map(this::buildDetail).collect(Collectors.toList());
        Map<String, Object> resp = new HashMap<>();
        resp.put("list", result);
        resp.put("total", result.size());
        return resp;
    }

    public Map<String, Object> getDetail(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) return null;
        return buildDetail(p);
    }

    // ES搜索（当前用MySQL LIKE实现）
    public Map<String, Object> search(String keyword, String tags, int page, int size) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
            .eq(Product::getStatus, ProductStatus.PUBLISHED.name())
            .orderByDesc(Product::getPublishedAt);

        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(Product::getTitle, keyword).or().like(Product::getDescription, keyword));
        }

        List<Product> list = productMapper.selectList(qw);

        // 标签过滤
        if (tags != null && !tags.isEmpty()) {
            String[] tagArr = tags.split(",");
            list = list.stream().filter(p -> {
                List<ProductTag> ptags = tagMapper.selectList(
                    new LambdaQueryWrapper<ProductTag>().eq(ProductTag::getProductId, p.getId())
                );
                return ptags.stream().anyMatch(t -> Arrays.asList(tagArr).contains(t.getTagName()));
            }).collect(Collectors.toList());
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, list.size());
        List<Map<String, Object>> result = list.subList(Math.min(start, list.size()), end)
            .stream().map(this::buildDetail).collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("list", result);
        resp.put("total", list.size());
        return resp;
    }
}
