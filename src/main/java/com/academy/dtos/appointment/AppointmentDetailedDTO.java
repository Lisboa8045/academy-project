package com.academy.dtos.appointment;

import com.academy.dtos.service.ServiceResponseDTO;

public record AppointmentDetailedDTO(
        Long id,
        String serviceProviderUsername,
        String memberUsername,
        Integer rating,
        String comment,
        String serviceName//TODO trocar para ser um ServiceDTO que tem apenas a info necessário para o ServiceComponent

) {
}
