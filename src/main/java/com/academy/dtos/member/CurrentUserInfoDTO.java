package com.academy.dtos.member;

public record CurrentUserInfoDTO(
        String username,
        long id,
        String profilePicture,
        String role
) {}