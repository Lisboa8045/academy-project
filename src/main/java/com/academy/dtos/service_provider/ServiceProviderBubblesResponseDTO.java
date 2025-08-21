package com.academy.dtos.service_provider;

import org.springframework.core.io.Resource;

public record ServiceProviderBubblesResponseDTO(
        long id,
        String username,
        String profilePicture
) {}
