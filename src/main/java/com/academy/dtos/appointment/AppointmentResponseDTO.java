package com.academy.dtos.appointment;

public record AppointmentResponseDTO(
        Long id,
        Long serviceProviderId,
        Long memberId,
        Integer rating,
        String comment
) {}