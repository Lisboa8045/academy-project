package com.academy.dtos.availability;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

public record AvailabilityRequestDTO(

    @NotNull(message = "MemberId cannot be null")
    Long memberId,

    @NotNull(message = "DayOfWeek cannot be null")
    DayOfWeek dayOfWeek,

    @NotNull(message = "StartDateTime cannot be null")
    LocalDateTime startDateTime,

    @NotNull(message = "EndDateTime cannot be null")
    LocalDateTime endDateTime
) {
    @AssertTrue(message = "EndDateTime must be after StartDateTime")
    public boolean isEndAfterStart() {
        if (startDateTime == null || endDateTime == null) {
            return true; // deixa o @NotNull tratar destes casos
        }
        return endDateTime.isAfter(startDateTime);
    }
}
