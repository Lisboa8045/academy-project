package com.academy.services;

import com.academy.dtos.SlotDTO;
import com.academy.models.appointment.Appointment;
import com.academy.models.availability.Availability;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
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
    private final ServiceService serviceService;

    @Autowired
    public SchedulingService(
            AvailabilityService availabilityService,
            AppointmentService appointmentService,
            ServiceProviderService serviceProviderService,
            MemberService memberService,
            ServiceService serviceService
    ) {
        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
        this.serviceProviderService = serviceProviderService;
        this.memberService = memberService;
        this.serviceService = serviceService;

    }

    public List<SlotDTO> getFreeSlotsForService(Long serviceId) {

        validateServiceId(serviceId);

        int serviceDurationMinutes = serviceService.getById(serviceId).duration();

        List<SlotDTO> allFreeSlots = new ArrayList<>();

        List<ServiceProvider> providersWithServePermission = serviceProviderService
                .findProvidersByServiceIdAndPermission(serviceId, ProviderPermissionEnum.SERVE);

        for (ServiceProvider serviceProvider : providersWithServePermission) {
            Long providerId = serviceProvider.getProvider().getId();

            Optional<Member> memberOpt = memberService.findbyId(providerId);
            if (memberOpt.isEmpty()) {
                continue;
            }

            Member member = memberOpt.get();

            List<Availability> allAvailabilities = availabilityService.getAllAvailabilitiesEntity();
            List<Availability> availabilities = allAvailabilities.stream()
                    .filter(av -> av.getMemberAvailabilities().stream()
                            .anyMatch(ma -> ma.getMember().getId().equals(providerId)))
                    .toList();

            List<Appointment> appointments = appointmentService.getAppointmentsForServiceProvider(
                    serviceProvider.getId()
            );

            List<SlotDTO> freeSlots = generateFreeSlots(
                    providerId,
                    member.getUsername(),
                    availabilities,
                    appointments,
                    serviceDurationMinutes
            );
            allFreeSlots.addAll(freeSlots);
        }
        return allFreeSlots;
    }


    private List<SlotDTO> generateFreeSlots(Long providerId, String providerName,
                                            List<Availability> availabilities,
                                            List<Appointment> appointments,
                                            int slotDurationMinutes) {
        List<SlotDTO> freeSlots = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Availability availability : availabilities) {
            for (var memberAvailability : availability.getMemberAvailabilities()) {
                if (!memberAvailability.getMember().getId().equals(providerId)) continue;

                List<LocalDate> dates = memberAvailability.getDates();
                if (dates == null || dates.isEmpty()) continue;

                for (LocalDate date : dates) {
                    // Usa startTime e endTime com a data
                    LocalDateTime start = date.atTime(availability.getStartTime());
                    LocalDateTime end = date.atTime(availability.getEndTime());

                    List<SlotDTO> slots;
                    try {
                        slots = SchedulingService.generateCompleteSlots(
                                providerId,
                                providerName,
                                start,
                                end,
                                slotDurationMinutes
                        );
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    for (SlotDTO slot : slots) {
                        if (slot.start().isBefore(now.minusSeconds(1))) continue;
                        if (isSlotFree(slot, appointments)) freeSlots.add(slot);
                    }
                }
            }
        }

        return freeSlots;
    }



    public static List<SlotDTO> generateCompleteSlots(Long providerId, String providerName,
                                                      LocalDateTime start, LocalDateTime end, int slotDurationMinutes) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Start and end dates cannot be null.");
        if (!end.isAfter(start))
            throw new IllegalArgumentException("The end date must be after the start date.");
        if (slotDurationMinutes <= 0)
            throw new IllegalArgumentException("Slot duration must be a positive value.");

        long totalDuration = Duration.between(start, end).toMinutes();
        if (slotDurationMinutes > totalDuration) {
            throw new IllegalArgumentException(
                    "Slot duration is greater than the total interval. No slots can be created.");
        }

        List<SlotDTO> slots = new ArrayList<>();
        LocalDateTime slotStart = start;
        LocalDateTime slotEnd = slotStart.plusMinutes(slotDurationMinutes);

        // Remove the buffer and restore the original condition
        while (!slotEnd.isAfter(end)) {
            slots.add(new SlotDTO(providerId, providerName, slotStart, slotEnd));
            slotStart = slotEnd;
            slotEnd = slotStart.plusMinutes(slotDurationMinutes);
        }

        return slots;
    }

    private void validateServiceId(Long serviceId) {
        if (serviceId == null) {
            throw new IllegalArgumentException("Service ID cannot be null");
        }
        if (!serviceProviderService.existsByServiceId(serviceId)) {
            throw new IllegalArgumentException("Service with ID " + serviceId + " does not exist");
        }
    }

    private boolean isSlotFree(SlotDTO slot, List<Appointment> appointments) {
        if (slot == null || slot.start() == null || slot.end() == null) return false;

        return appointments.stream()
                .filter(app -> app.getStatus() != AppointmentStatus.CANCELLED)
                .noneMatch(app ->
                        app.getStartDateTime() != null &&
                                app.getEndDateTime() != null &&
                                // Comprehensive overlap detection
                                (slot.start().isBefore(app.getEndDateTime()) &&
                                        slot.end().isAfter(app.getStartDateTime()))
                );
    }

}
