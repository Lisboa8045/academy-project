package com.academy.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academy.models.Availability;
import com.academy.repositories.AvailabilityRepository;
import com.academy.repositories.ServiceProviderRepository;

import jakarta.transaction.Transactional;

@Service
public class AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository; // Repository for service_providers table

    // Create a new availability
    public Availability createAvailability(Availability availability) {
        return availabilityRepository.save(availability);
    }

    // Get all availabilities for a specific member by their ID
    public List<Availability> getAvailabilitiesByMemberId(Long memberId) {
        return availabilityRepository.findByMember_Id(memberId);
    }

    // Get all availabilities for a specific service by its ID
    public List<Availability> getAvailabilitiesByServiceId(Long serviceId) {
   
        List<Long> memberIds = serviceProviderRepository.findMemberIdsByServiceId(serviceId);

        // Collect all availabilities for the members
        List<Availability> availabilities = new ArrayList<>();
        for (Long memberId : memberIds) {
            availabilities.addAll(getAvailabilitiesByMemberId(memberId));
        }

        return availabilities;
    }

    // Get a specific availability by its ID
    public Availability getAvailabilityById(Long availabilityId) {
        return availabilityRepository.findById(availabilityId).orElse(null);
    }

    // Update an existing availability
    @Transactional
    public Availability updateAvailability(Availability availability) {
        if (!availabilityRepository.existsById(availability.getId())) {
            throw new IllegalArgumentException("Availability with ID " + availability.getId() + " does not exist.");
        }
        return availabilityRepository.save(availability);
    }

    // Delete an availability by its ID
    @Transactional
    public void deleteAvailabilityById(Long availabilityId) {
        if (!availabilityRepository.existsById(availabilityId)) {
            throw new IllegalArgumentException("Availability with ID " + availabilityId + " does not exist.");
        }
        availabilityRepository.deleteById(availabilityId);
    }

    // Check if an availability exists by its ID
    public boolean existsById(Long availabilityId) {
        return availabilityRepository.existsById(availabilityId);
    }

    // Get all availabilities
    public List<Availability> getAllAvailabilities() {
        return availabilityRepository.findAll();
    }
}