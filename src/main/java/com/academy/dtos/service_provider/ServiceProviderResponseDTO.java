package com.academy.dtos.service_provider;

import com.academy.dtos.appointment.AppointmentReviewResponseDTO;
import com.academy.models.service.service_provider.ProviderPermissionEnum;

import java.util.List;

public record ServiceProviderResponseDTO(
        long id,
        String memberName,
        long serviceId,
        List<AppointmentReviewResponseDTO> appointmentReviewList,
        List<ProviderPermissionEnum> permissions,
        boolean active
) {}