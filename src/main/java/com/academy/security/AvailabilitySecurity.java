package com.academy.security;

import com.academy.services.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AvailabilitySecurity {

    private final AvailabilityService availabilityService;

    @Autowired
    public AvailabilitySecurity(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    public boolean isOwner(Long availabilityId, String username) {
        return availabilityService.isAvailabilityOwnedByUser(availabilityId, username);
    }
}
