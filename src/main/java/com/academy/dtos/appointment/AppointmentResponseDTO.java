package com.academy.dtos.appointment;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        Long serviceProviderId, //TODO isto não deve estar aqui
        String serviceProviderUsername,
        Long memberId, //TODO isto não deve estar aqui
        String memberUsername,
        Integer rating,
        String comment,
        LocalDateTime startDateTime,
        String serviceName
) {}
