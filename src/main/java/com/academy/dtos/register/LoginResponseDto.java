package com.academy.dtos.register;

public record LoginResponseDto(String message, String token ,long memberId, String username, String profilePicture, String role) {
}
