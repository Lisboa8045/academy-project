package com.academy.dtos.notification;

import com.academy.models.notification.NotificationTypeEnum;

public record NotificationResponseDTO(
        long id,
        long memberId,
        String title,
        String body,
        String url,
        boolean seen,
        NotificationTypeEnum type
) {
}
