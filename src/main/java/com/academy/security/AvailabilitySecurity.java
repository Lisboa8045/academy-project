package com.academy.security;

import com.academy.repositories.AvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AvailabilitySecurity {

    private final AvailabilityRepository availabilityRepository;

    @Autowired
    public AvailabilitySecurity(AvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    public boolean isOwner(Long availabilityId, String username) {
        return availabilityRepository.findById(availabilityId)
                .map(availability -> username.equals(availability.getMember().getUsername()))
                .orElse(false);
    }
}
