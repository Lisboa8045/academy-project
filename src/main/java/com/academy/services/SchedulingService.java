package com.academy.services;

import com.academy.dtos.SlotDTO;
import com.academy.models.Appointment;
import com.academy.models.Availability;
import com.academy.models.Member;
import com.academy.utils.SlotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SchedulingService {
    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;
    private final ServiceProviderService serviceProviderService;
    private final MemberService memberService;

    @Value("${slot.duration.minutes:30}")
    private int slotDurationMinutes;

    @Autowired
    public SchedulingService(
            AvailabilityService availabilityService,
            AppointmentService appointmentService,
            ServiceProviderService serviceProviderService,
            MemberService memberService
    ) {
        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
        this.serviceProviderService = serviceProviderService;
        this.memberService = memberService;
    }

   public List<SlotDTO> getFreeSlotsForService(Long serviceId) {
        if (serviceId == null) {
            throw new IllegalArgumentException("Service ID cannot be null");
        }

        boolean serviceExists = serviceProviderService.existsByServiceId(serviceId);
        if (!serviceExists) {
            throw new IllegalArgumentException("Service with ID " + serviceId + " does not exist");
        }

        List<SlotDTO> allFreeSlots = new ArrayList<>();
        List<Long> providerIds = serviceProviderService.findMemberIdsByServiceId(serviceId);

        System.out.println("[DEBUG] Providers for service " + serviceId + ": " + providerIds);

        for (Long providerId : providerIds) {
            Optional<Member> memberOpt = memberService.findbyId(providerId);
            String providerName = memberOpt.isPresent() ? memberOpt.get().getUsername() : "Unknown";
            System.out.println("[DEBUG] Processing provider: " + providerId + " (" + providerName + ")");

            // Fetch all future availabilities for this provider
            List<Availability> availabilities = availabilityService.getAvailabilitiesForProvider(providerId);
            System.out.println("[DEBUG] Availabilities for provider " + providerId + ": " + availabilities.size());

            // Fetch all future appointments for this provider
            List<Appointment> appointments = appointmentService.getAppointmentsForProvider(providerId);
            System.out.println("[DEBUG] Appointments for provider " + providerId + ": " + appointments.size());

            for (Availability availability : availabilities) {
                List<SlotDTO> slots = SlotUtils.generateSlots(
                    providerId,
                    providerName,
                    availability.getStartDateTime(),
                    availability.getEndDateTime(),
                    slotDurationMinutes
                );
                for (SlotDTO slot : slots) {
                    // Only consider slots that start in the future
                    if (slot.getStart().isBefore(LocalDateTime.now())) {
                        continue;
                    }
                    boolean occupied = appointments.stream().anyMatch(app ->
                        app.getStartDateTime().isBefore(slot.getEnd()) && app.getEndDateTime().isAfter(slot.getStart())
                    );
                    if (!occupied) {
                        allFreeSlots.add(slot);
                    }
                }
            }
        }
        System.out.println("[DEBUG] Total free slots found: " + allFreeSlots.size());
        return allFreeSlots;
    }
}