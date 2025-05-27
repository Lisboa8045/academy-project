package com.academy.dtos.appointment;

import jakarta.validation.constraints.*;

public record AppointmentRequestDTO(
        @NotNull Long serviceProviderId,
        @NotNull Long memberId,
        @Min(0) @Max(5) Integer rating,
        @Size(max = 400) String comment
) {}