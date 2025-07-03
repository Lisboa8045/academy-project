package com.academy.dtos.tag;

import com.academy.utils.FieldLengths;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TagRequestDTO(
        @NotBlank @Size(max = FieldLengths.TAG_NAME_MAX) String name,
        @NotNull Boolean custom,
        List<Long> serviceIds
) {}