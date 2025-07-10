package com.academy.dtos.appointment;

import java.time.LocalDateTime;

import com.academy.models.appointment.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        Long serviceProviderId,
        String serviceProviderUsername,
        Long memberId,
        String memberUsername,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer rating,
        String comment,
        AppointmentStatus status,
        String serviceName,
        double price,
        int duration
        ) {}
