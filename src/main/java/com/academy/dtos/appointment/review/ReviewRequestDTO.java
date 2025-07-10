package com.academy.dtos.appointment.review;

import com.academy.utils.FieldLengths;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewRequestDTO(
        @Min(1) @Max(5) Integer rating,
        @Size(max = FieldLengths.REVIEW_MAX) String comment) {}