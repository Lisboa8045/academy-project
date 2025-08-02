package com.academy.dtos.notification;

import com.academy.models.notification.NotificationTypeEnum;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        long id,
        long memberId,
        String title,
        String body,
        String url,
        boolean seen,
        NotificationTypeEnum type,
        LocalDateTime createdAt
) {
}
