package com.academy.dtos.service_type;

import com.academy.dtos.service.ServiceResponseDTO;
import java.util.List;

public record ServiceTypeResponseDTO (
        Long id,
        String name,
        String icon,
        List<ServiceResponseDTO> services
) {}
