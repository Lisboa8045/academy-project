package com.academy.appointment;

import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.dtos.service_type.ServiceTypeResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Role;
import com.academy.models.ServiceType;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.member.Member;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.RoleRepository;
import com.academy.repositories.ServiceProviderRepository;
import com.academy.services.AppointmentService;

import com.academy.services.MemberService;
import com.academy.services.ServiceProviderService;
import com.academy.services.ServiceService;
import com.academy.services.ServiceTypeService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Transactional
@SpringBootTest
class AppointmentIntegrationTests {

    @Autowired private AppointmentService appointmentService;

    @Autowired private ServiceService serviceService;

    @Autowired private ServiceProviderService serviceProviderService;

    @Autowired private ServiceTypeService serviceTypeService;

    @Autowired private ServiceProviderRepository serviceProviderRepository;

    @Autowired private RoleRepository roleRepository;

    private Role defaultRole;
    private ServiceType defaultServiceType;
    @Autowired
    private MemberService memberService;

    @BeforeEach
    void setup() {
        defaultRole = new Role();
        defaultRole.setName("USER");
        defaultRole = roleRepository.save(defaultRole);

        defaultServiceType = new ServiceType();
        ServiceTypeRequestDTO serviceTypeRequestDTO = new ServiceTypeRequestDTO("Mecânico", "Icon");
        ServiceTypeResponseDTO createdDto = serviceTypeService.create(serviceTypeRequestDTO);
        defaultServiceType = serviceTypeService.getServiceTypeEntityById(createdDto.id()).orElseThrow();

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

        // Set authentication for the test user
        mockAuthentication("teste1");
    }

    @Test
    void testCreateValidAppointment() throws BadRequestException {
        mockAuthentication("teste1");

        Member provider = createAndSaveProvider("provider1");
        Service service = createAndSaveServiceWithDuration(provider, 60);
        ServiceProvider sp = createAndSaveServiceProviderWithServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusMinutes(service.getDuration());

        AppointmentRequestDTO dto = new AppointmentRequestDTO(
                sp.getId(), start, end, null, null, AppointmentStatus.PENDING
        );

        AppointmentResponseDTO created = appointmentService.createAppointment(dto);

        assertNotNull(created);
        assertEquals(start, created.startDateTime());
        assertEquals(AppointmentStatus.PENDING, created.status());
        assertEquals(sp.getId(), created.serviceProviderId());

        assertTrue(appointmentService.getAppointmentById(created.id()).isPresent());
    }

    @Test
    void testAppointmentWithNoServePermissionThrows() throws BadRequestException {
        mockAuthentication("teste1");

        Member provider = createAndSaveProvider("provider2");
        Service service = createAndSaveServiceWithDuration(provider, 45);
        ServiceProvider sp = createAndSaveServiceProviderWithoutServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0);
        LocalDateTime end = start.plusMinutes(service.getDuration());

        AppointmentRequestDTO dto = new AppointmentRequestDTO(
                sp.getId(), start, end, null, null, AppointmentStatus.PENDING
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> appointmentService.createAppointment(dto));

        // Update to match actual error message
        assertEquals("Service provider doesn't have SERVE permission", exception.getMessage());
    }

    @Test
    void testAppointmentInPastThrows() throws BadRequestException {
        mockAuthentication("teste1");

        Member provider = createAndSaveProvider("provider4");
        Service service = createAndSaveServiceWithDuration(provider, 30);
        ServiceProvider sp = createAndSaveServiceProviderWithServePermission(provider, service);

        // Make sure the date is clearly in the past
        LocalDateTime start = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusMinutes(service.getDuration());

        AppointmentRequestDTO dto = new AppointmentRequestDTO(
                sp.getId(), start, end, null, null, AppointmentStatus.PENDING
        );

        // Verify the exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.createAppointment(dto));
    }
    @Test
    void testAppointmentWithConflictThrows() throws BadRequestException {
        mockAuthentication("teste1");

        Member provider = createAndSaveProvider("provider3");
        Service service = createAndSaveServiceWithDuration(provider, 30);
        ServiceProvider sp = createAndSaveServiceProviderWithServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
        LocalDateTime end = start.plusMinutes(service.getDuration());

        // First appointment should succeed
        AppointmentRequestDTO dto1 = new AppointmentRequestDTO(
                sp.getId(), start, end, null, null, AppointmentStatus.CONFIRMED
        );
        appointmentService.createAppointment(dto1);

        // Verify conflict detection
        assertThrows(IllegalStateException.class, () -> {
            // Exact same time slot
            AppointmentRequestDTO dto2 = new AppointmentRequestDTO(
                    sp.getId(), start, end, null, null, AppointmentStatus.PENDING
            );
            appointmentService.createAppointment(dto2);
        });

        assertThrows(IllegalStateException.class, () -> {
            // Overlapping time slot
            LocalDateTime conflictStart = start.plusMinutes(15);
            LocalDateTime conflictEnd = conflictStart.plusMinutes(service.getDuration());
            AppointmentRequestDTO dto3 = new AppointmentRequestDTO(
                    sp.getId(), conflictStart, conflictEnd, null, null, AppointmentStatus.PENDING
            );
            appointmentService.createAppointment(dto3);
        });
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
        // First set authentication to the provider (using username, not email)
        mockAuthentication(provider.getUsername());

        ServiceRequestDTO dto = new ServiceRequestDTO(
                "Serviço Teste",
                "Descrição de Teste",
                30.0,
                0,
                false,
                duration,
                defaultServiceType.getName(),
                new ArrayList<>()
        );

        return serviceService.createToEntity(dto);
    }

    private ServiceProvider createAndSaveServiceProviderWithServePermission(Member provider, Service service) throws BadRequestException {
        try {
            // Try to get existing service provider using the service
            try {
                return serviceProviderService.getServiceProviderByProviderIdAndServiceID(provider.getId(), service.getId());
            } catch (EntityNotFoundException e) {
                // Not found, so create a new one
                ServiceProviderRequestDTO dto = new ServiceProviderRequestDTO(
                        provider.getId(),
                        service.getId(),
                        List.of(ProviderPermissionEnum.SERVE),
                        true
                );
                return serviceProviderService.createServiceProvider(dto);
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to create service provider with serve permission", e);
        }
    }


    private ServiceProvider createAndSaveServiceProviderWithoutServePermission(Member provider, Service service) throws BadRequestException {
        try {
            ServiceProvider serviceProvider;

            try {
                // Try to get existing service provider using the service
                serviceProvider = serviceProviderService.getServiceProviderByProviderIdAndServiceID(provider.getId(), service.getId());

                // If it exists and has no permissions, return it
                if (serviceProvider.getPermissions().isEmpty()) {
                    return serviceProvider;
                }

                // Otherwise clear the permissions
                serviceProvider.setPermissions(Collections.emptyList());

            } catch (EntityNotFoundException e) {
                // Not found, so create a new one
                ServiceProviderRequestDTO dto = new ServiceProviderRequestDTO(
                        provider.getId(),
                        service.getId(),
                        Collections.emptyList(),
                        true
                );
                serviceProvider = serviceProviderService.createServiceProvider(dto);
            }

            return serviceProvider;
        } catch (Exception e) {
            throw new BadRequestException("Failed to create service provider without serve permission", e);
        }
    }


    private void mockAuthentication(String username) {
        var authentication = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
