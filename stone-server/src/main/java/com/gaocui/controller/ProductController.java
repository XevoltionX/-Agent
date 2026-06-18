package com.gaocui.controller;

import com.gaocui.common.Result;
import com.gaocui.dto.PublishProductDTO;
import com.gaocui.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 发布商品
    @PostMapping("/products/publish")
    public Result<?> publish(@RequestAttribute("merchantId") Long merchantId,
                             @RequestAttribute("isVip") Boolean isVip,
                             @RequestBody PublishProductDTO dto) {
        return Result.ok(productService.publish(merchantId, isVip != null && isVip, dto));
    }

    // 编辑商品
    @PutMapping("/products/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody PublishProductDTO dto) {
        return Result.ok(productService.update(id, dto));
    }

    // 删除商品
    @DeleteMapping("/products/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }

    // 上下架
    @PutMapping("/products/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        productService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    // 商家自己的商品列表
    @GetMapping("/products")
    public Result<?> list(@RequestAttribute("merchantId") Long merchantId,
                          @RequestParam(required = false) String status) {
        return Result.ok(productService.getMerchantProducts(merchantId, status));
    }

    // 商品详情（公开）
    @GetMapping("/products/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(productService.getDetail(id));
    }

}
