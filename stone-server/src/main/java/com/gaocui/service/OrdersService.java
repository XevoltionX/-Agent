package com.gaocui.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.entity.Merchant;
import com.gaocui.entity.Orders;
import com.gaocui.mapper.MerchantMapper;
import com.gaocui.mapper.OrdersMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrdersService {

    private final OrdersMapper ordersMapper;
    private final MerchantMapper merchantMapper;

    public OrdersService(OrdersMapper ordersMapper, MerchantMapper merchantMapper) {
        this.ordersMapper = ordersMapper;
        this.merchantMapper = merchantMapper;
    }

    // VIP价格表
    public static final Map<String, BigDecimal> PLANS = Map.of(
        "12month", new BigDecimal("2999"),
        "6month", new BigDecimal("1688")
    );

    public Orders createOrder(Long merchantId, String planType) {
        BigDecimal price = PLANS.getOrDefault(planType, new BigDecimal("2999"));
        Orders order = new Orders();
        order.setOrderNo(UUID.randomUUID().toString().substring(0, 16));
        order.setMerchantId(merchantId);
        order.setPlanType(planType);
        order.setTotal(price);
        order.setStatus("待支付");
        ordersMapper.insert(order);
        return order;
    }

    public Orders getByOrderNo(String orderNo) {
        return ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getOrderNo, orderNo));
    }

    // 支付成功 → 升级VIP
    public void handlePaySuccess(String orderNo, String tradeNo, String gmtPayment) {
        Orders order = getByOrderNo(orderNo);
        if (order == null || !"待支付".equals(order.getStatus())) return;

        order.setStatus("已支付");
        order.setPayNo(tradeNo);
        order.setPayTime(gmtPayment);
        ordersMapper.updateById(order);

        // 升级商家为VIP
        Merchant m = merchantMapper.selectById(order.getMerchantId());
        if (m == null) return;

        int months = "12month".equals(order.getPlanType()) ? 12 : 6;
        LocalDate start = m.getVipStartDate() != null ? m.getVipStartDate() : LocalDate.now();
        if (m.getIsVip() != null && m.getIsVip() == 1) {
            // 续费：从当前到期日延长
            start = m.getVipEndDate() != null ? m.getVipEndDate() : LocalDate.now();
        }
        m.setIsVip(1);
        m.setVipStartDate(start);
        m.setVipEndDate(start.plusMonths(months));
        merchantMapper.updateById(m);
    }
}
