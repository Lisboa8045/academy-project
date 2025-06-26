package com.academy.dtos.notification;

import com.academy.models.notification.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class NotificationMapper {

    public abstract NotificationResponseDTO toDTO(Notification notification);
}
