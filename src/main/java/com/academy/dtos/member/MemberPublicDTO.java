package com.academy.dtos.member;

public record MemberPublicDTO(
        String username,
        String profilePicture,
        String role
) {}