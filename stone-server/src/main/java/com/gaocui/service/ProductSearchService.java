package com.gaocui.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
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

@Slf4j
@Service
public class ProductSearchService {

    private static final String INDEX_NAME = "gaocui_products";

    private final ElasticsearchClient esClient;
    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;
    private final ProductTagMapper tagMapper;

    public ProductSearchService(ElasticsearchClient esClient, ProductMapper productMapper,
                                ProductImageMapper imageMapper, ProductTagMapper tagMapper) {
        this.esClient = esClient;
        this.productMapper = productMapper;
        this.imageMapper = imageMapper;
        this.tagMapper = tagMapper;
    }

    // 全量重建索引
    public int reindexAll() {
        try {
            // 创建或覆盖索引
            boolean exists = esClient.indices().exists(e -> e.index(INDEX_NAME)).value();
            if (exists) {
                esClient.indices().delete(d -> d.index(INDEX_NAME));
            }
            esClient.indices().create(c -> c.index(INDEX_NAME)
                .settings(s -> s.numberOfShards("1").numberOfReplicas("0"))
                .mappings(m -> m.properties("title", p -> p.text(t -> t.analyzer("ik_max_word").searchAnalyzer("ik_smart")))
                                    .properties("description", p -> p.text(t -> t.analyzer("ik_max_word").searchAnalyzer("ik_smart")))
                                    .properties("tags", p -> p.keyword(k -> k))
                                    .properties("price", p -> p.double_(d -> d))
                                    .properties("status", p -> p.keyword(k -> k))
                ));

            // 只索引已上架商品
            List<Product> products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getStatus, ProductStatus.PUBLISHED.name())
            );

            if (products.isEmpty()) return 0;

            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
            for (Product p : products) {
                Map<String, Object> doc = buildDoc(p);
                bulkBuilder.operations(op -> op.index(idx -> idx.index(INDEX_NAME).id(p.getId().toString()).document(doc)));
            }

            BulkResponse bulkResp = esClient.bulk(bulkBuilder.build());
            int count = 0;
            for (BulkResponseItem item : bulkResp.items()) {
                if (item.error() == null) count++;
            }
            log.info("ES索引重建完成：{}件商品", count);
            return count;
        } catch (Exception e) {
            log.error("ES索引重建失败", e);
            return -1;
        }
    }

    // 索引单个商品
    public void indexProduct(Product p, List<String> images, List<String> tags) {
        try {
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", p.getId());
            doc.put("merchantId", p.getMerchantId());
            doc.put("title", p.getTitle());
            doc.put("description", p.getDescription() != null ? p.getDescription() : "");
            doc.put("price", p.getPrice());
            doc.put("tags", tags);
            doc.put("images", images);
            doc.put("status", p.getStatus());
            doc.put("publishedAt", p.getPublishedAt() != null ? p.getPublishedAt().toString() : null);
            esClient.index(i -> i.index(INDEX_NAME).id(p.getId().toString()).document(doc));
        } catch (Exception e) {
            log.error("ES索引商品失败 id={}", p.getId(), e);
        }
    }

    // 从ES删除
    public void deleteProduct(Long productId) {
        try {
            esClient.delete(d -> d.index(INDEX_NAME).id(productId.toString()));
        } catch (Exception e) {
            log.error("ES删除商品失败 id={}", productId, e);
        }
    }

    // ES搜索
    public Map<String, Object> search(String keyword, String tagsStr, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            BoolQuery.Builder bool = new BoolQuery.Builder();

            // 只搜已上架
            bool.filter(Query.of(q -> q.term(t -> t.field("status").value("PUBLISHED"))));

            // IK分词 + 标签wildcard
            if (keyword != null && !keyword.isEmpty()) {
                bool.should(Query.of(q -> q.match(m -> m.field("title").query(keyword))));
                bool.should(Query.of(q -> q.match(m -> m.field("description").query(keyword))));
                bool.should(Query.of(q -> q.wildcard(w -> w.field("tags").value("*" + keyword + "*"))));
                bool.minimumShouldMatch("1");
            }

            // 标签过滤
            if (tagsStr != null && !tagsStr.isEmpty()) {
                String[] tags = tagsStr.split(",");
                for (String tag : tags) {
                    bool.filter(Query.of(q -> q.term(t -> t.field("tags").value(tag.trim()))));
                }
            }

            int from = (page - 1) * size;
            SearchResponse<Map> response = esClient.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(bool.build()))
                .from(from)
                .size(size),
                Map.class
            );

            List<Map<String, Object>> list = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                Map<String, Object> source = hit.source();
                if (source != null) list.add(source);
            }

            result.put("list", list);
            result.put("total", response.hits().total() != null ? response.hits().total().value() : 0);
        } catch (Exception e) {
            log.error("ES搜索失败", e);
            result.put("list", Collections.emptyList());
            result.put("total", 0);
        }
        return result;
    }

    private Map<String, Object> buildDoc(Product p) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", p.getId());
        doc.put("merchantId", p.getMerchantId());
        doc.put("title", p.getTitle());
        doc.put("description", p.getDescription() != null ? p.getDescription() : "");
        doc.put("price", p.getPrice());
        doc.put("tags", getTags(p.getId()));
        doc.put("images", getImages(p.getId()));
        doc.put("status", p.getStatus());
        doc.put("publishedAt", p.getPublishedAt() != null ? p.getPublishedAt().toString() : null);
        return doc;
    }

    private List<String> getTags(Long productId) {
        return tagMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductTag>()
                .eq(ProductTag::getProductId, productId).orderByAsc(ProductTag::getSortOrder)
        ).stream().map(ProductTag::getTagName).collect(Collectors.toList());
    }

    private List<String> getImages(Long productId) {
        return imageMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId).orderByAsc(ProductImage::getSortOrder)
        ).stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
    }
}
