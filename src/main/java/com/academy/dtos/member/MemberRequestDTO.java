package com.academy.dtos.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberRequestDTO(
        @NotBlank String username,
        @NotBlank String email,
        String address,
        String postalCode,
        String phoneNumber,
        @NotBlank String password,
        @NotNull Long roleId
) {}