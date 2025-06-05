package com.academy.dtos.member;

import com.academy.util.FieldLengths;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberRequestDTO(
        @NotBlank @Size(max = FieldLengths.EMAIL_MAX) String email,
        @Size(max = FieldLengths.ADDRESS_MAX) String address,
        @Size(max = FieldLengths.POSTAL_CODE_MAX) String postalCode,
        @Size(max = FieldLengths.PHONE_NUMBER_MAX) String phoneNumber,
        @NotBlank @Size(max = FieldLengths.PASSWORD_MAX) String password,
        @NotNull Long roleId
) {}