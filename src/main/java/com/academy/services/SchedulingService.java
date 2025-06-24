package com.academy.services;

import com.academy.dtos.SlotDTO;
import com.academy.models.appointment.Appointment;
import com.academy.models.Availability;
import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
        validateServiceId(serviceId);

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

            List<Availability> availabilities = availabilityService.getAvailabilitiesForProvider(providerId);
            List<Appointment> appointments = appointmentService.getAppointmentsForProvider(providerId);

            List<SlotDTO> freeSlots = generateFreeSlots(
                    providerId,
                    member.getUsername(),
                    availabilities,
                    appointments
            );

            allFreeSlots.addAll(freeSlots);
        }

        return allFreeSlots;
    }

    public static List<SlotDTO> generateCompleteSlots(Long providerId, String providerName,
                                                      LocalDateTime start, LocalDateTime end, int slotDurationMinutes) {
        if (start == null || end == null)
            throw new IllegalArgumentException("Datas entre início e fim não podem ser nulas.");
        if (!end.isAfter(start))
            throw new IllegalArgumentException("Data final deve ser maior que início.");
        if (slotDurationMinutes <= 0)
            throw new IllegalArgumentException("Duração dos slots deve ser positiva.");

        long totalDuration = Duration.between(start, end).toMinutes();
        if (slotDurationMinutes > totalDuration) {
            throw new IllegalArgumentException(
                    "Duração do slot maior do que o intervalo total. Nenhum slot pode ser criado.");
        }

        List<SlotDTO> slots = new ArrayList<>();
        LocalDateTime slotStart = start;
        LocalDateTime slotEnd = slotStart.plusMinutes(slotDurationMinutes);

        while (!slotEnd.isAfter(end)) { // Garante que o slot termina dentro do intervalo
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

    private List<SlotDTO> generateFreeSlots(Long providerId, String providerName,
                                            List<Availability> availabilities,
                                            List<Appointment> appointments) {
        List<SlotDTO> freeSlots = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Availability availability : availabilities) {
            List<SlotDTO> slots = SchedulingService.generateCompleteSlots(
                    providerId,
                    providerName,
                    availability.getStartDateTime(),
                    availability.getEndDateTime(),
                    slotDurationMinutes
            );

            for (SlotDTO slot : slots) {
                if (slot.start().isBefore(now)) {
                    continue;
                }
                if (isSlotFree(slot, appointments)) {
                    freeSlots.add(slot);
                }
            }
        }
        return freeSlots;
    }

    private boolean isSlotFree(SlotDTO slot, List<Appointment> appointments) {
        return appointments.stream().noneMatch(app ->
                app.getStartDateTime().isBefore(slot.end()) &&
                        app.getEndDateTime().isAfter(slot.start())
        );
    }
}
