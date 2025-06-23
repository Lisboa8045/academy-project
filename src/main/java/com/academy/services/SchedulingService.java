package com.academy.services;

import com.academy.dtos.SlotDTO;
import com.academy.models.Appointment;
import com.academy.models.Availability;
import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
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

    @Value("${slot.duration.minutes:60}")
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
        List<Availability> availabilities = new ArrayList<>();

        for (Long providerId : providerIds) {
            Optional<Member> memberOpt = memberService.findbyId(providerId);
            String providerName = memberOpt.isPresent() ? memberOpt.get().getUsername() : "Unknown";

            ServiceProvider serviceProvider = serviceProviderService
                    .getServiceProviderByProviderIdAndServiceID(providerId, serviceId);

            boolean hasServePermission = serviceProvider.getPermissions().stream()
                    .map(ProviderPermission::getPermission)
                    .anyMatch(permission -> permission == ProviderPermissionEnum.SERVE);

            //ALTERAR para hasServePermissions
            if (hasServePermission) {
                availabilities = availabilityService.getAvailabilitiesForProvider(providerId);
            }

            // Fetch all future appointments for this provider
            List<Appointment> appointments = appointmentService.getAppointmentsForProvider(providerId);

            for (Availability availability : availabilities) {
                List<SlotDTO> slots = SlotUtils.generateCompleteSlots(
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