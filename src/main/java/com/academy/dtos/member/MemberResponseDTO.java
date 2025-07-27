package com.academy.dtos.member;

public record MemberResponseDTO(
        long id,
        String username,
        String email,
        String address,
        String postalCode,
        String phoneNumber,
        String role,
        String profilePicture
) {}