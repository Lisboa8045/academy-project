package com.academy.dtos.service_provider;

import com.academy.dtos.appointment.AppointmentReviewResponseDTO;
import java.util.List;

public record ServiceProviderResponseDTO(
        long id,
        String memberName,
        long serviceId,
        List<AppointmentReviewResponseDTO> appointmentReviewList,
        String permission
) {}