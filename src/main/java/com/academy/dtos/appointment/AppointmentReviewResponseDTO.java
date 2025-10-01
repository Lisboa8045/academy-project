package com.academy.dtos.appointment;

import java.time.LocalDate;

public record AppointmentReviewResponseDTO(
        Long id,
        Long memberId,
        Integer rating,
        String comment,
        String memberUsername,
        String memberProfilePicture,
        LocalDate appointmentDate
) {}