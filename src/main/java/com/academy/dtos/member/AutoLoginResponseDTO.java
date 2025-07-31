package com.academy.dtos.member;

public record AutoLoginResponseDTO(
        long id,
        String username,
        String profilePicture,
        String role
) {}