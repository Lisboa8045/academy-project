package com.academy.security;

import com.academy.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationSecurity {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationSecurity(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public boolean isOwner(Long notificationId, String username) {
        return notificationRepository.findById(notificationId)
                .map(notification -> notification.getMember().getUsername().equals(username))
                .orElse(false);
    }
}
