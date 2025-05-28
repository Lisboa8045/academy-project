package com.academy.dtos.member;

import jakarta.validation.constraints.NotNull;

public record MemberRequestDTO(
        @NotNull String email,
        String address,
        String postalCode,
        String phoneNumber,
        @NotNull String password,
        @NotNull Long roleId
) {}
