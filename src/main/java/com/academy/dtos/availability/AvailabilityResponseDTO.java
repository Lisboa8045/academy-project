package com.academy.dtos.availability;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public record AvailabilityResponseDTO(
        Long id,
        Long memberId,
        DayOfWeek dayOfWeek,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        boolean isException
) {}