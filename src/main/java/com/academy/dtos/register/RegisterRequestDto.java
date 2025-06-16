package com.academy.dtos.register;

import com.academy.util.FieldLengths;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank @Size(max = FieldLengths.USERNAME_MAX) String username,
        @NotBlank @Size(max = FieldLengths.PASSWORD_MAX) String password,
        @NotBlank @Size(max = FieldLengths.EMAIL_MAX) String email,
        @NotNull Long roleId,
        @Size(max = FieldLengths.ADDRESS_MAX) String address,
        @Size(max = FieldLengths.POSTAL_CODE_MAX) String postalCode,
        @Size(max = FieldLengths.PHONE_NUMBER_MAX) String phoneNumber
) {}
