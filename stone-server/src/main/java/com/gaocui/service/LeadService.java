package com.gaocui.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.common.BizException;
import com.gaocui.entity.Lead;
import com.gaocui.entity.Notification;
import com.gaocui.enums.LeadStatus;
import com.gaocui.enums.NotifyType;
import com.gaocui.mapper.LeadMapper;
import com.gaocui.mapper.NotificationMapper;
import com.gaocui.mapper.ProductMapper;
import com.gaocui.entity.Product;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeadService {

    private final LeadMapper leadMapper;
    private final NotificationMapper notificationMapper;
    private final ProductMapper productMapper;

    public LeadService(LeadMapper leadMapper, NotificationMapper notificationMapper, ProductMapper productMapper) {
        this.leadMapper = leadMapper;
        this.notificationMapper = notificationMapper;
        this.productMapper = productMapper;
    }

    private Map<String, Object> buildDetail(Lead l, Map<Long, Product> productMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", l.getId());
        map.put("merchantId", l.getMerchantId());
        map.put("productId", l.getProductId());
        map.put("buyer_email", l.getBuyerEmail());      // snake_case 前端用
        map.put("buyerEmail", l.getBuyerEmail());
        map.put("buyer_message", l.getBuyerMessage());   // snake_case 前端用
        map.put("buyerMessage", l.getBuyerMessage());
        map.put("status", l.getStatus());
        map.put("created_at", l.getCreatedAt() != null ? l.getCreatedAt().toString().replace("T", " ").substring(0, 16) : "");
        map.put("createdAt", l.getCreatedAt());
        Product p = productMap.get(l.getProductId());
        map.put("product_title", p != null ? p.getTitle() : "");  // snake_case 前端用
        map.put("productTitle", p != null ? p.getTitle() : "");
        return map;
    }

    public Map<String, Object> getLeads(Long merchantId, String status, int page, int size) {
        return getLeadsPaged(merchantId, status, page, size);
    }

    public Map<String, Object> getLeads(Long merchantId, String status) {
        LambdaQueryWrapper<Lead> qw = new LambdaQueryWrapper<Lead>()
            .eq(Lead::getMerchantId, merchantId)
            .orderByDesc(Lead::getCreatedAt);
        if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
            qw.eq(Lead::getStatus, status.toUpperCase());
        }
        List<Lead> list = leadMapper.selectList(qw);

        // 批量查关联商品（一次查询，避免N+1）
        Set<Long> productIds = list.stream().map(Lead::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectBatchIds(productIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        }

        Map<Long, Product> finalProductMap = productMap;
        List<Map<String, Object>> result = list.stream().map(l -> buildDetail(l, finalProductMap)).collect(Collectors.toList());
        Map<String, Object> resp = new HashMap<>();
        resp.put("list", result);
        resp.put("total", result.size());
        return resp;
    }

    public Map<String, Object> getDetail(Long id) {
        Lead l = leadMapper.selectById(id);
        if (l == null) throw new BizException("客资不存在");
        Map<Long, Product> productMap = new HashMap<>();
        if (l.getProductId() != null) {
            Product p = productMapper.selectById(l.getProductId());
            productMap.put(l.getProductId(), p);
        }
        return buildDetail(l, productMap);
    }

    public void markContacted(Long id) {
        Lead l = leadMapper.selectById(id);
        if (l == null) throw new BizException("客资不存在");
        l.setStatus(LeadStatus.CONTACTED.name());
        leadMapper.updateById(l);
    }

    private Map<String, Object> getLeadsPaged(Long merchantId, String status, int page, int size) {
        return getLeads(merchantId, status); // 暂时全量返回，前端自行分页
    }

    // 买家提交留资（公开接口）
    public void submitInquiry(Long productId, String buyerEmail, String buyerMessage) {
        Product p = productMapper.selectById(productId);
        if (p == null) throw new BizException("商品不存在");

        Lead lead = new Lead();
        lead.setMerchantId(p.getMerchantId());
        lead.setProductId(productId);
        lead.setBuyerEmail(buyerEmail);
        lead.setBuyerMessage(buyerMessage);
        lead.setStatus(LeadStatus.PENDING.name());
        leadMapper.insert(lead);

        // 发送通知给商家
        Notification notif = new Notification();
        notif.setMerchantId(p.getMerchantId());
        notif.setType(NotifyType.NEW_LEAD.name());
        notif.setTitle("您有一条新的客户留资——" + p.getTitle());
        notif.setIsRead(0);
        notificationMapper.insert(notif);
    }
}
