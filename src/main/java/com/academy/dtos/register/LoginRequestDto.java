package com.academy.dtos.register;

import com.academy.utils.FieldLengths;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank @Size(max = FieldLengths.EMAIL_MAX) String login,
        @NotBlank @Size(max = FieldLengths.PASSWORD_MAX) String password) {
}
