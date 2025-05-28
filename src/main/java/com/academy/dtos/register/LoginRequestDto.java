package com.academy.dtos.register;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank String login,
        @NotBlank String password) {
}
