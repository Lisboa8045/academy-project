package com.academy.services;

import com.academy.dtos.notification.NotificationResponseDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(Long memberId, NotificationResponseDTO notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + memberId,
                notification
        );
    }
}
