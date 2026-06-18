package com.gaocui.controller;

import com.gaocui.common.Result;
import com.gaocui.service.LeadService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    // 客资列表（分页+懒加载）
    @GetMapping("/leads")
    public Result<?> list(@RequestAttribute("merchantId") Long merchantId,
                          @RequestParam(defaultValue = "all") String status,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(leadService.getLeads(merchantId, status, page, size));
    }

    // 客资详情
    @GetMapping("/leads/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(leadService.getDetail(id));
    }

    // 标记已联系
    @PutMapping("/leads/{id}/contact")
    public Result<?> markContacted(@PathVariable Long id) {
        leadService.markContacted(id);
        return Result.ok();
    }

    // 买家提交留资（公开）
    @PostMapping("/inquiry")
    public Result<?> submitInquiry(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        String email = (String) body.get("email");
        String message = (String) body.get("message");
        leadService.submitInquiry(productId, email, message);
        return Result.ok();
    }
}
