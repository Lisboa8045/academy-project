package com.academy.dtos.appointment;
import jakarta.validation.constraints.*;

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
