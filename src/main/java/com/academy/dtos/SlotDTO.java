package com.academy.dtos;

import java.time.LocalDateTime;

public record SlotDTO(Long providerId, String providerName, LocalDateTime start, LocalDateTime end) {
}
