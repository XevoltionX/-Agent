package com.gaocui.controller;

import com.gaocui.common.Result;
import com.gaocui.service.ProductSearchService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "gaocui.search.mode", havingValue = "elasticsearch")
public class EsController {

    private final ProductSearchService searchService;

    public EsController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    // 重建ES索引
    @PostMapping("/admin/reindex")
    public Result<?> reindex() {
        int count = searchService.reindexAll();
        return Result.ok("已索引 " + count + " 件商品");
    }

    // ES搜索（公开）
    @GetMapping("/search")
    public Result<?> search(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String tags,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(searchService.search(keyword, tags, page, size));
    }
}
