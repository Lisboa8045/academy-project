package com.academy.dtos.service_type;

import com.academy.util.FieldLengths;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceTypeRequestDTO (
    @NotBlank @Size(max = FieldLengths.SERVICE_TYPE_MAX) String name,
    @NotBlank @Size(max = FieldLengths.URL_MAX) String icon
) {}
