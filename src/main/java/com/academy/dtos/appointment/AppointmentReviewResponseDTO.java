package com.academy.dtos.appointment;

public record AppointmentReviewResponseDTO(
        Long id,
        Integer rating,
        String comment,
        String memberUsername
) {}