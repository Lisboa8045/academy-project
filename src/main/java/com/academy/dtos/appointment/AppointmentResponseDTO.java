package com.academy.dtos.appointment;

import com.academy.models.appointment.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        Long memberId,
        Long serviceProviderId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer rating,
        String comment,
        AppointmentStatus status
) {}
