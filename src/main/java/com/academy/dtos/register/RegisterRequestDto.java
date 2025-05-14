package com.academy.dtos.register;

import jakarta.validation.constraints.NotEmpty;

public record RegisterRequestDto(
        @NotEmpty String username,
        @NotEmpty String password,
        @NotEmpty String email,
        @NotEmpty long roleId,
        String address,
        String postalCode,
        String phoneNumber
) {}
