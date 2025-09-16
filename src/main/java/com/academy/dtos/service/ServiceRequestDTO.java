package com.academy.dtos.service;

import com.academy.utils.FieldLengths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ServiceRequestDTO(
        @NotBlank @Size(max = FieldLengths.SERVICE_TITLE_MAX) String name,
        @NotBlank @Size(max = FieldLengths.SERVICE_DESCRIPTION_MAX) String description,
        @Positive double price,
        @Min(value = 0, message = "cannot be less than 0%")
        @Max(value = 100, message = "cannot exceed 100%")
        int discount,
        boolean negotiable,
        @Positive int duration,
        @NotBlank @Size(max = FieldLengths.SERVICE_ENTITY_MAX) String entity,
        @NotBlank @Size(max = FieldLengths.SERVICE_TYPE_MAX) String serviceTypeName,

        @Size(max = FieldLengths.MAX_SERVICE_TAGS)
        @Valid
        List<@Size(max = FieldLengths.TAG_NAME_MAX) String> tagNames
) {}
