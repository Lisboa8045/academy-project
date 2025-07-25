package com.academy.dtos.availability;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public record DefaultAvailabilityRequest(
        @NotNull Set<DayOfWeek> days,
        @NotNull LocalTime morningStartTime,
        @NotNull LocalTime morningEndTime,
        @NotNull LocalTime afternoonStartTime,
        @NotNull LocalTime afternoonEndTime
) {
    public DefaultAvailabilityRequest {
        Objects.requireNonNull(days, "Days cannot be null");
        Objects.requireNonNull(morningStartTime, "Morning start time cannot be null");
        Objects.requireNonNull(morningEndTime, "Morning end time cannot be null");
        Objects.requireNonNull(afternoonStartTime, "Afternoon start time cannot be null");
        Objects.requireNonNull(afternoonEndTime, "Afternoon end time cannot be null");

        if (!morningEndTime.isAfter(morningStartTime)) {
            throw new IllegalArgumentException("Morning end time must be after start time");
        }
        if (!afternoonEndTime.isAfter(afternoonStartTime)) {
            throw new IllegalArgumentException("Afternoon end time must be after start time");
        }
        if (afternoonStartTime.isBefore(morningEndTime)) {
            throw new IllegalArgumentException("Afternoon start must be after morning end");
        }
    }
}