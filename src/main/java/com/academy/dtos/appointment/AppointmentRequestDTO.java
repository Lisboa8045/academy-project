package com.academy.dtos.appointment;

import com.academy.util.FieldLengths;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppointmentRequestDTO(
        @NotNull Long serviceProviderId,
        @NotNull Long memberId,
        @Min(0) @Max(5) Integer rating,
        @Size(max = FieldLengths.REVIEW_MAX) String comment
) {}
