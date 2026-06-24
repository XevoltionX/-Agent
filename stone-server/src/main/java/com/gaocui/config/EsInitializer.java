package com.gaocui.config;

import com.gaocui.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "gaocui.search.mode", havingValue = "elasticsearch")
public class EsInitializer implements CommandLineRunner {

    private final ProductSearchService searchService;

    public EsInitializer(ObjectProvider<ProductSearchService> searchServiceProvider) {
        this.searchService = searchServiceProvider.getIfAvailable();
    }

    @Override
    public void run(String... args) {
        if (searchService == null) return;
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
