package com.academy.dtos.appointment;

import com.academy.models.appointment.AppointmentStatus;
import com.academy.utils.FieldLengths;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequestDTO(
        @NotNull Long serviceProviderId,
        @NotNull LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime,
        @Min(1) @Max(5) Integer rating,
        @Size(max = FieldLengths.REVIEW_MAX) String comment,
        AppointmentStatus status // Isto está a ser ignorado em bastantes sítios, apesar de ser passado é ignorado
) {}
