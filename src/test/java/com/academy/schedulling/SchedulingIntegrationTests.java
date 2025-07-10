package com.academy.schedulling;

import com.academy.dtos.SlotDTO;
import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.member.Member;
import com.academy.models.Role;
import com.academy.models.ServiceType;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.RoleRepository;
import com.academy.services.AppointmentService;
import com.academy.services.AvailabilityService;
import com.academy.services.MemberService;
import com.academy.services.SchedulingService;
import com.academy.services.ServiceProviderService;
import com.academy.services.ServiceService;
import com.academy.services.ServiceTypeService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Transactional
@SpringBootTest
public class SchedulingIntegrationTests {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ServiceProviderService serviceProviderService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ServiceTypeService serviceTypeService;

    private Role defaultRole;
    private ServiceType defaultServiceType;

    @BeforeEach
    void setup() {
        defaultRole = new Role();
        defaultRole.setName("USER");
        defaultRole = roleRepository.save(defaultRole);

        ServiceTypeRequestDTO serviceTypeRequestDTO = new ServiceTypeRequestDTO("Mecanico", "Icon");
        defaultServiceType = serviceTypeService.createToEntity(serviceTypeRequestDTO);

        RegisterRequestDto registerRequest = new RegisterRequestDto(
                "teste1",
                "Teste123.",
                "teste1@example.com",
                defaultRole.getId(),
                "Rua Teste 1",
                "1000-100",
                "912345678"
        );

        memberService.register(registerRequest);

        var authentication = new UsernamePasswordAuthenticationToken("teste1", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testSlotDurationMatchServiceDuration() throws BadRequestException {
        Member provider = createAndSaveProvider("slotmatch");
        Service service = createAndSaveServiceWithDuration(provider, 45); // 45 minutes

        // Create service provider with permission
        ServiceProvider sp = createAndSaveServiceProviderWithServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        AvailabilityRequestDTO avDto = new AvailabilityRequestDTO(
                provider.getId(),
                start.getDayOfWeek(),
                start,
                start.plusMinutes(90) // 1h30
        );
        availabilityService.createAvailability(avDto);

        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(service.getId());

        assertNotNull(slots);
        assertEquals(2, slots.size());
        assertEquals(45, Duration.between(slots.get(0).start(), slots.get(0).end()).toMinutes());
        assertEquals(45, Duration.between(slots.get(1).start(), slots.get(1).end()).toMinutes());
    }

    @Test
    void testNoSlotsIfAvailabilityLessThanServiceDuration() throws BadRequestException {
        Member provider = createAndSaveProvider("noslotscase");
        Service service = createAndSaveServiceWithDuration(provider, 60); // 1h
        createAndSaveServiceProviderWithServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusHours(3);
        AvailabilityRequestDTO avDto = new AvailabilityRequestDTO(
                provider.getId(),
                start.getDayOfWeek(),
                start,
                start.plusMinutes(40) // less than 1h
        );
        availabilityService.createAvailability(avDto);

        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(service.getId());
        assertTrue(slots.isEmpty());
    }

    @Test
    void testSlotsWithOverlapWithAppointmentAreFiltered() throws BadRequestException {
        Member provider = createAndSaveProvider("overlapteste");
        Service service = createAndSaveServiceWithDuration(provider, 30);
        ServiceProvider serviceProvider = createAndSaveServiceProviderWithServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MINUTES);
        AvailabilityRequestDTO avDto = new AvailabilityRequestDTO(
                provider.getId(),
                start.getDayOfWeek(),
                start,
                start.plusHours(2)
        );
        availabilityService.createAvailability(avDto);

        // Create appointment
        LocalDateTime appointmentStart = start.plusMinutes(30);
        AppointmentRequestDTO appointmentDto = new AppointmentRequestDTO(
                serviceProvider.getId(),
                appointmentStart,
                appointmentStart.plusMinutes(30),
                null,
                null,
                AppointmentStatus.CONFIRMED
        );
        appointmentService.createAppointment(appointmentDto);

        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(service.getId());

        // Verify no overlapping slots
        boolean anyOverlap = slots.stream().anyMatch(slot ->
                slot.start().isBefore(appointmentStart.plusMinutes(30)) &&
                        slot.end().isAfter(appointmentStart)
        );

        assertFalse(anyOverlap, "No slots should overlap with appointment. Found " + slots.size() + " slots.");
    }

    private Member createAndSaveProvider(String username) {
        RegisterRequestDto registerRequest = new RegisterRequestDto(
                username,
                "senha123A!",
                username + "@example.com",
                defaultRole.getId(),
                "Rua Teste 1",
                "1000-100",
                "912345678"
        );

        long id = memberService.register(registerRequest);
        return memberService.getMemberEntityById(id);
    }

    private Service createAndSaveServiceWithDuration(Member provider, int duration) throws BadRequestException {
        ServiceRequestDTO serviceDto = new ServiceRequestDTO(
                "Serviço Teste",
                "Teste descrição",
                20.1,
                0,
                false,
                duration,
                defaultServiceType.getName(),
                new ArrayList<>()
        );

        // Need to set authentication to the provider
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(provider.getUsername(), null, Collections.emptyList())
            );
            return serviceService.createToEntity(serviceDto);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(current);
        }
    }

    private ServiceProvider createAndSaveServiceProviderWithServePermission(Member provider, Service service) throws BadRequestException {
        try {
            // Try to get existing service provider
            return serviceProviderService.getServiceProviderByProviderIdAndServiceID(provider.getId(), service.getId());
        } catch (EntityNotFoundException e) {
            // Create new one if not exists
            ServiceProviderRequestDTO dto = new ServiceProviderRequestDTO(
                    provider.getId(),
                    service.getId(),
                    List.of(ProviderPermissionEnum.SERVE),
                    true
            );
            return serviceProviderService.createServiceProvider(dto);
        }
    }

    public static void mockAuthentication(String username) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}