package com.academy.appointment;

import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.models.*;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.member.Member;
import com.academy.models.member.MemberStatusEnum;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.*;
import com.academy.services.AppointmentService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class AppointmentIntegrationTests {

    @Autowired private AppointmentService appointmentService;

    @Autowired private AppointmentRepository appointmentRepository;

    @Autowired private MemberRepository memberRepository;

    @Autowired private ServiceRepository serviceRepository;

    @Autowired private ServiceProviderRepository serviceProviderRepository;

    @Autowired private ServiceTypeRepository serviceTypeRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private ProviderPermissionRepository providerPermissionRepository;

    @Autowired private TagRepository tagRepository;

    private Role defaultRole;
    private ServiceType defaultServiceType;

    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
        serviceProviderRepository.deleteAll();
        serviceRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        memberRepository.deleteAll();
        roleRepository.deleteAll();

        defaultRole = new Role();
        defaultRole.setName("USER");
        defaultRole = roleRepository.save(defaultRole);

        defaultServiceType = new ServiceType();
        defaultServiceType.setName("Mecânico");
        defaultServiceType.setIcon("Icon");
        defaultServiceType = serviceTypeRepository.save(defaultServiceType);

        Member user = new Member();
        user.setUsername("teste1");
        user.setEmail("teste1@example.com");
        user.setPassword("Teste123.");
        user.setRole(defaultRole);
        user.setEnabled(true);
        user.setStatus(MemberStatusEnum.ACTIVE);
        user.setAddress("Rua Teste 1");
        user.setPostalCode("1000-100");
        user.setPhoneNumber("912345678");
        memberRepository.save(user);

        var authentication = new UsernamePasswordAuthenticationToken("teste1", "Teste123.", List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();

        appointmentRepository.deleteAll();
        providerPermissionRepository.deleteAll();
        serviceProviderRepository.deleteAll();

        serviceRepository.findAll().forEach(service -> service.getTags().clear());
        serviceRepository.saveAll(serviceRepository.findAll());

        serviceRepository.deleteAll();
        tagRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        memberRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void testCreateValidAppointment() {
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

        assertTrue(appointmentRepository.findById(created.id()).isPresent());
    }

    @Test
    void testAppointmentWithNoServePermissionThrows() {
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
        assertEquals("Service provider não possui permissão SERVE", exception.getMessage());
    }

    @Test
    void testAppointmentInPastThrows() {
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
    void testAppointmentWithConflictThrows() {
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
        Member provider = new Member();
        provider.setUsername(username);
        provider.setEmail(username + "@example.com");
        provider.setPassword("senha123A!");
        provider.setRole(defaultRole);
        provider.setEnabled(true);
        provider.setStatus(MemberStatusEnum.ACTIVE);
        provider.setAddress("Rua Teste 1");
        provider.setPostalCode("1000-100");
        provider.setPhoneNumber("912345678");
        return memberRepository.save(provider);
    }

    private Service createAndSaveServiceWithDuration(Member provider, int duration) {
        Service service = new Service();
        service.setName("Serviço Teste");
        service.setDescription("Descrição de Teste");
        service.setDuration(duration);
        service.setOwner(provider);
        service.setServiceType(defaultServiceType);
        service.setPrice(30.0);
        service.setDiscount(0);
        service.setNegotiable(false);
        service.setTags(new ArrayList<>());
        return serviceRepository.save(service);
    }

    private ServiceProvider createAndSaveServiceProviderWithServePermission(Member provider, Service service) {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProvider(provider);
        serviceProvider.setService(service);

        ProviderPermission permission = new ProviderPermission();
        permission.setServiceProvider(serviceProvider);
        permission.setPermission(ProviderPermissionEnum.SERVE);

        serviceProvider.setPermissions(List.of(permission));
        serviceProviderRepository.save(serviceProvider);

        return serviceProvider;
    }

    private ServiceProvider createAndSaveServiceProviderWithoutServePermission(Member provider, Service service) {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProvider(provider);
        serviceProvider.setService(service);
        serviceProvider.setPermissions(Collections.emptyList());
        return serviceProviderRepository.save(serviceProvider);
    }

    private void mockAuthentication(String username) {
        var authentication = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
