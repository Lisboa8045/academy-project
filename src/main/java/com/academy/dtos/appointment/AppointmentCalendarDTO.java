package com.academy.dtos.appointment;

import com.academy.models.appointment.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentCalendarDTO(
    String memberUsername,
    String serviceName,
    LocalDateTime startDateTime,
    LocalDateTime endDateTime,
    AppointmentStatus status
) {}
