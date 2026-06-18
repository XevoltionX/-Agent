package com.gaocui.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.gaocui.common.Result;
import com.gaocui.config.AliPayConfig;
import com.gaocui.entity.Orders;
import com.gaocui.service.OrdersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/alipay")
public class AliPayController {

    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    private final AliPayConfig aliPayConfig;
    private final OrdersService ordersService;

    public AliPayController(AliPayConfig aliPayConfig, OrdersService ordersService) {
        this.aliPayConfig = aliPayConfig;
        this.ordersService = ordersService;
    }

    // 创建订单 → 返回orderNo
    @PostMapping("/create")
    public Result<?> createOrder(@RequestBody Map<String, String> body,
                                  @RequestAttribute("merchantId") Long merchantId) {
        String planType = body.getOrDefault("planType", "12month");
        Orders order = ordersService.createOrder(merchantId, planType);
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("total", order.getTotal());
        return Result.ok(result);
    }

    // 跳转支付宝支付页面
    @GetMapping("/pay")
    public void pay(@RequestParam String orderNo, HttpServletResponse httpResponse) throws Exception {
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) return;

        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(), FORMAT, CHARSET, aliPayConfig.getAlipayPublicKey(), SIGN_TYPE);

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());

        Map<String, Object> bizContent = new LinkedHashMap<>();
        bizContent.put("out_trade_no", order.getOrderNo());
        bizContent.put("total_amount", order.getTotal().toString());
        bizContent.put("subject", order.getPlanType().equals("12month") ? "VIP会员(12个月)" : "VIP会员(6个月)");
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        request.setBizContent(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(bizContent));
        request.setReturnUrl("http://localhost:5173/merchant/account");

        String form = alipayClient.pageExecute(request).getBody();
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    // 支付宝异步通知（natapp穿透 → 本地）
    @PostMapping("/notify")
    public void payNotify(HttpServletRequest request) throws Exception {
        String tradeStatus = request.getParameter("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)) return;

        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }

        String sign = params.get("sign");
        String content = AlipaySignature.getSignCheckContentV1(params);
        boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, aliPayConfig.getAlipayPublicKey(), "UTF-8");

        if (checkSignature) {
            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String gmtPayment = params.get("gmt_payment");
            log.info("支付宝支付成功: orderNo={}, tradeNo={}, amount={}", orderNo, tradeNo, params.get("total_amount"));
            ordersService.handlePaySuccess(orderNo, tradeNo, gmtPayment);
        }
    }
}
