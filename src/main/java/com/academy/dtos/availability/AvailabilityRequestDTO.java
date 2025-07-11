package com.academy.dtos.availability;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

public record AvailabilityRequestDTO(
        @NotNull Long memberId,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime
) {
    @AssertTrue
    public boolean isEndAfterStart() {
        return endDateTime.isAfter(startDateTime);
    }
}