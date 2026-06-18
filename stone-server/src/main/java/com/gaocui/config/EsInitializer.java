package com.gaocui.config;

import com.gaocui.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EsInitializer implements CommandLineRunner {

    private final ProductSearchService searchService;

    public EsInitializer(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public void run(String... args) {
        try {
            int count = searchService.reindexAll();
            if (count >= 0) {
                log.info("ES索引初始化完成：{}件商品", count);
            }
        } catch (Exception e) {
            log.warn("ES初始化失败（可能ES未启动）: {}", e.getMessage());
        }
    }
}
