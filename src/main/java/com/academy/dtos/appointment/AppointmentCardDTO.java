package com.academy.dtos.appointment;

import java.time.LocalDateTime;

public record AppointmentCardDTO(
        Long id,
        String serviceProviderUsername,
        String memberUsername,
        String serviceName,
        LocalDateTime startDateTime
) {
}
