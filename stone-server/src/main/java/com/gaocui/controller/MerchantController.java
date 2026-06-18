package com.gaocui.controller;

import com.gaocui.common.Result;
import com.gaocui.service.MerchantService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping("/merchant/account")
    public Result<?> account(@RequestAttribute("merchantId") Long merchantId) {
        return Result.ok(merchantService.getAccount(merchantId));
    }

    @GetMapping("/merchant/publish-quota")
    public Result<?> publishQuota(@RequestAttribute("merchantId") Long merchantId) {
        return Result.ok(merchantService.getPublishQuota(merchantId));
    }

    // 后台看板轻量数据
    @GetMapping("/merchant/dashboard")
    public Result<?> dashboard(@RequestAttribute("merchantId") Long merchantId,
                                @RequestAttribute("isVip") Boolean isVip) {
        return Result.ok(merchantService.getDashboard(merchantId, isVip != null && isVip));
    }
}
