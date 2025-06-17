package com.academy.dtos.appointment;

import com.academy.util.FieldLengths;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequestDTO(
        @NotNull Long serviceProviderId,
        @NotNull LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime,
        @Min(0) @Max(5) Integer rating,
        @Size(max = 400) String comment,
        String status
) {
}
