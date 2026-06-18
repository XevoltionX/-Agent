package com.gaocui.controller;

import com.gaocui.common.Result;
import com.gaocui.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public Result<?> list(@RequestAttribute("merchantId") Long merchantId) {
        return Result.ok(notificationService.getNotifications(merchantId));
    }

    @PutMapping("/notifications/{id}/read")
    public Result<?> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.ok();
    }

    @GetMapping("/notifications/unread-count")
    public Result<?> unreadCount(@RequestAttribute("merchantId") Long merchantId) {
        return Result.ok(notificationService.getUnreadCount(merchantId));
    }

    @PutMapping("/notifications/read-all")
    public Result<?> markAllRead(@RequestAttribute("merchantId") Long merchantId) {
        notificationService.markAllRead(merchantId);
        return Result.ok();
    }
}
