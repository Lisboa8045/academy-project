package com.academy.dtos.appointment;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        Long memberId,
        Long serviceProviderId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer rating,
        String comment
) {}
