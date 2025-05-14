package com.academy.dtos.register;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequestDto(
        @NotEmpty String username,
        @NotEmpty String password) {
}
