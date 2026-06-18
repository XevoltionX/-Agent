package com.gaocui.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaocui.entity.Notification;
import com.gaocui.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public List<Notification> getNotifications(Long merchantId) {
        return notificationMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getMerchantId, merchantId)
                .orderByDesc(Notification::getCreatedAt)
        );
    }

    public void markRead(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    public void markAllRead(Long merchantId) {
        List<Notification> list = notificationMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getMerchantId, merchantId).eq(Notification::getIsRead, 0)
        );
        for (Notification n : list) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    public long getUnreadCount(Long merchantId) {
        return notificationMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification>()
                .eq(Notification::getMerchantId, merchantId)
                .eq(Notification::getIsRead, 0)
        );
    }
}
