package com.academy.dtos.register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDto(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String email,
        @NotNull Long roleId,
        String address,
        String postalCode,
        String phoneNumber
) {}
