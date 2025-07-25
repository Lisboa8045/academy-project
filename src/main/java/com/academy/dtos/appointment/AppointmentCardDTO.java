package com.academy.dtos.appointment;

import com.academy.models.appointment.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentCardDTO(
        Long id,
        String serviceProviderUsername,
        String memberUsername,
        String serviceName,
        LocalDateTime startDateTime,
        AppointmentStatus status
) {
}
