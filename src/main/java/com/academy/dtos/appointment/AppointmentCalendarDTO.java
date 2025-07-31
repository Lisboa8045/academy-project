package com.academy.dtos.appointment;

import com.academy.models.appointment.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentCalendarDTO(
        Long id,
        String memberUsername,
        String serviceName,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Double price,
        AppointmentStatus status
) {}
