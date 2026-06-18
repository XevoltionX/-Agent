package com.gaocui.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.entity.Merchant;
import com.gaocui.entity.Notification;
import com.gaocui.enums.NotifyType;
import com.gaocui.mapper.MerchantMapper;
import com.gaocui.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class VipExpiryScheduler {

    private final MerchantMapper merchantMapper;
    private final NotificationMapper notificationMapper;

    public VipExpiryScheduler(MerchantMapper merchantMapper, NotificationMapper notificationMapper) {
        this.merchantMapper = merchantMapper;
        this.notificationMapper = notificationMapper;
    }

    // 每天8点检查VIP到期
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkVipExpiry() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);

        List<Merchant> vips = merchantMapper.selectList(
            new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getIsVip, 1)
                .eq(Merchant::getStatus, 1)
                .le(Merchant::getVipEndDate, thirtyDaysLater)
                .ge(Merchant::getVipEndDate, today)
        );

        for (Merchant vip : vips) {
            // 检查今天是否已发过通知
            Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getMerchantId, vip.getId())
                    .eq(Notification::getType, NotifyType.VIP_EXPIRY.name())
                    .ge(Notification::getCreatedAt, today.atStartOfDay())
            );

            if (count == 0) {
                Notification n = new Notification();
                n.setMerchantId(vip.getId());
                n.setType(NotifyType.VIP_EXPIRY.name());
                n.setTitle("VIP会员即将到期提醒");
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, vip.getVipEndDate());
                n.setContent("您的VIP会员将于" + daysLeft + "天后（" + vip.getVipEndDate() + "）到期，请及时续费以继续享受VIP权益");
                n.setIsRead(0);
                notificationMapper.insert(n);
                log.info("VIP到期提醒已发送: merchant={}, daysLeft={}", vip.getId(), daysLeft);
            }
        }
    }
}
