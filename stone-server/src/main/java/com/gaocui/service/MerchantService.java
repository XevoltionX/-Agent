package com.gaocui.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.common.BizException;
import com.gaocui.entity.Lead;
import com.gaocui.entity.Merchant;
import com.gaocui.entity.Product;
import com.gaocui.enums.ProductStatus;
import com.gaocui.mapper.LeadMapper;
import com.gaocui.mapper.MerchantMapper;
import com.gaocui.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MerchantService {

    private final MerchantMapper merchantMapper;
    private final ProductMapper productMapper;
    private final LeadMapper leadMapper;

    public MerchantService(MerchantMapper merchantMapper, ProductMapper productMapper, LeadMapper leadMapper) {
        this.merchantMapper = merchantMapper;
        this.productMapper = productMapper;
        this.leadMapper = leadMapper;
    }

    public Map<String, Object> getAccount(Long merchantId) {
        Merchant m = merchantMapper.selectById(merchantId);
        if (m == null) throw new BizException("商家不存在");

        boolean isVip = m.getIsVip() != null && m.getIsVip() == 1;
        int maxProducts = isVip ? 100 : 2;
        long publishedCount = productMapper.selectCount(
            new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId).eq(Product::getStatus, ProductStatus.PUBLISHED.name())
        );

        Map<String, Object> result = new HashMap<>();
        result.put("isVip", isVip);
        result.put("vipStartDate", m.getVipStartDate());
        result.put("vipEndDate", m.getVipEndDate());
        result.put("maxProducts", maxProducts);
        result.put("publishedCount", publishedCount);
        return result;
    }

    public Map<String, Object> getDashboard(Long merchantId, boolean isVip) {
        Map<String, Object> result = new HashMap<>();
        int maxProducts = isVip ? 100 : 2;
        long publishedCount = productMapper.selectCount(
            new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId).eq(Product::getStatus, "PUBLISHED")
        );
        result.put("productCount", publishedCount + "/" + maxProducts);
        result.put("maxProducts", maxProducts);

        // 今日客资（简化：统计所有pending的）
        long todayLeads = leadMapper.selectCount(
            new LambdaQueryWrapper<Lead>().eq(Lead::getMerchantId, merchantId)
        );
        long totalLeads = todayLeads;
        result.put("todayLeads", (int) todayLeads);
        result.put("totalLeads", (int) totalLeads);
        return result;
    }

    public Map<String, Object> getPublishQuota(Long merchantId) {
        Merchant m = merchantMapper.selectById(merchantId);
        boolean isVip = m.getIsVip() != null && m.getIsVip() == 1;
        int max = isVip ? 100 : 2;
        long used = productMapper.selectCount(
            new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId).in(Product::getStatus, ProductStatus.PUBLISHED.name(), ProductStatus.DRAFT.name())
        );

        Map<String, Object> result = new HashMap<>();
        result.put("max", max);
        result.put("used", used);
        result.put("remain", Math.max(0, max - used));
        return result;
    }
}
