package com.academy.dtos.member;

import com.academy.util.FieldLengths;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberRequestDTO(
        @NotBlank @Size(max = FieldLengths.USERNAME_MAX) String username,
        @NotBlank @Size(max = FieldLengths.EMAIL_MAX) String email,
        @Size(max = FieldLengths.ADDRESS_MAX) String address,
        @Size(max = FieldLengths.POSTAL_CODE_MAX)
        @Pattern(regexp = "^[0-9]{4}-[0-9]{3}$",
                message = "Postal code must be in the format 1234-567")
        String postalCode,
        @Size(max = FieldLengths.PHONE_NUMBER_MAX)
        @Pattern(regexp = "^\\+[0-9]{12}$",
                message = "Phone number must start with '+' and must contain 12 digits") String phoneNumber,
        @NotNull Long roleId,
        @NotBlank @Size(max = FieldLengths.PASSWORD_MAX) String oldPassword,
        @NotBlank @Size(max = FieldLengths.PASSWORD_MAX) String newPassword
) {}