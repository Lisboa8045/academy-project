package com.academy.security;

import com.academy.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationSecurity {

    private final NotificationService notificationService;

    @Autowired
    public NotificationSecurity(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public boolean isNotificationOwnedByUser(Long notificationId, String username) {
        return notificationService.isNotificationOwnedByUser(notificationId, username);
    }
}
