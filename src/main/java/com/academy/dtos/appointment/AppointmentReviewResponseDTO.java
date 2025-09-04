package com.academy.dtos.appointment;

import java.time.LocalDateTime;

public record AppointmentReviewResponseDTO(
        Long id,
        Long memberId,
        Integer rating,
        String comment,
        String memberUsername,
        String memberProfilePicture,
        LocalDateTime createdAt
) {}