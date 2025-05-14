package com.academy.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.Availability;
import com.academy.repositories.AvailabilityRepository;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.ServiceProviderRepository;
import com.academy.repositories.ServiceRepository;

import jakarta.transaction.Transactional;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceRepository serviceRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public AvailabilityService (AvailabilityRepository availabilityRepository, 
                                    ServiceProviderRepository serviceProviderRepository, 
                                    MemberRepository memberRepository,
                                    ServiceRepository serviceRepository) {

        this.availabilityRepository = availabilityRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.memberRepository = memberRepository;
        this.serviceRepository = serviceRepository;
    }

    // Get all availabilities for a specific member by their ID
    public List<Availability> getAvailabilitiesByMemberId(Long memberId) {
        if (memberId == null) {
            throw new InvalidArgumentException("Member ID cannot be null");
        }
        if (!memberRepository.existsById(memberId)) {
            throw new InvalidArgumentException("Member with ID " + memberId + " does not exist.");
        }
        return availabilityRepository.findByMember_Id(memberId);
    }

    // Get all availabilities for a specific service by its ID
    public List<Availability> getAvailabilitiesByServiceId(Long serviceId) {

        if (serviceId == null) {
            throw new InvalidArgumentException("Service ID cannot be null");
        }
        if (!serviceRepository.existsById(serviceId)) {
            throw new InvalidArgumentException("Service with ID " + serviceId + " does not exist.");
        }
   
        List<Long> memberIds = serviceProviderRepository.findMemberIdsByServiceId(serviceId);

        System.out.println("Member IDs: " + memberIds);

        // Collect all availabilities for the members
        List<Availability> availabilities = new ArrayList<>();
        for (Long memberId : memberIds) {
            availabilities.addAll(getAvailabilitiesByMemberId(memberId));
        }

        return availabilities;
    }

    // Get a specific availability by its ID
    public Availability getAvailabilityById(Long availabilityId) {
        if (availabilityId == null) {
            throw new InvalidArgumentException("Availability ID cannot be null");
        }
        if (!availabilityRepository.existsById(availabilityId)) {
            throw new InvalidArgumentException("Availability with ID " + availabilityId + " does not exist.");
        }
        return availabilityRepository.findById(availabilityId).orElse(null);
    }

    
    @Transactional
    // Create a new availability
    public Availability createAvailability(Availability availability) {
        return availabilityRepository.save(availability);
    }

    // Update an existing availability
    @Transactional
    public Availability updateAvailability(Long id, Availability availability) {
        if (id == null) {
            throw new InvalidArgumentException("Availability ID cannot be null");
        }
        if (availability == null) {
            throw new InvalidArgumentException("Availability cannot be null");
        }	
        if (!availabilityRepository.existsById(id)) {
            throw new InvalidArgumentException("Availability with ID " + availability.getId() + " does not exist.");
        }
        return availabilityRepository.save(availability);
    }

    // Delete an availability by its ID
    @Transactional
    public void deleteAvailabilityById(Long availabilityId) {
        if (availabilityId == null) {
            throw new InvalidArgumentException("Availability ID cannot be null");
        }
        if (!availabilityRepository.existsById(availabilityId)) {
            throw new InvalidArgumentException("Availability with ID " + availabilityId + " does not exist.");
        }
        availabilityRepository.deleteById(availabilityId);
    }

    // Get all availabilities
    public List<Availability> getAllAvailabilities() {
        return availabilityRepository.findAll();
    }
}