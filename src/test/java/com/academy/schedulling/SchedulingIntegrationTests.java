package com.academy.schedulling;

import com.academy.dtos.SlotDTO;
import com.academy.models.appointment.Appointment;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.member.Member;
import com.academy.models.member.MemberStatusEnum;
import com.academy.models.Role;
import com.academy.models.availability.Availability;
import com.academy.models.ServiceType;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.*;
import com.academy.services.SchedulingService;
import com.academy.repositories.ServiceProviderRepository;

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

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
public class SchedulingIntegrationTests {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProviderPermissionRepository providerPermissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    private Role defaultRole;
    private ServiceType defaultServiceType;

    @BeforeEach
    void setup() {
        defaultRole = new Role();
        defaultRole.setName("USER");
        defaultRole = roleRepository.save(defaultRole);

        defaultServiceType = new ServiceType();
        defaultServiceType.setName("Mecanico");
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

        user = new Member();
        user.setUsername("testUser");
        user.setEmail("testUser@example.com");
        user.setPassword("Teste123.");
        user.setRole(defaultRole);
        user.setEnabled(true);
        user.setStatus(MemberStatusEnum.ACTIVE);
        memberRepository.save(user);

        var authentication = new UsernamePasswordAuthenticationToken("teste1", "Teste123.", List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testSlotDurationMatchServiceDuration() {
        mockAuthentication("testUser");

        Member provider = createAndSaveProvider("slotmatch");
        Service service = createAndSaveServiceWithDuration(provider, 45); // 45 minutes

        // Create and save service provider with permission
        ServiceProvider sp = new ServiceProvider();
        sp.setProvider(provider);
        sp.setService(service);
        sp.setActive(true);
        sp = serviceProviderRepository.save(sp);

        ProviderPermission permission = new ProviderPermission();
        permission.setServiceProvider(sp);
        permission.setPermission(ProviderPermissionEnum.SERVE);
        providerPermissionRepository.save(permission);

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        Availability av = new Availability();
        //av.setMember(provider);
        av.setStartDateTime(start);
        av.setEndDateTime(start.plusMinutes(90)); // 1h30
        availabilityRepository.save(av);

        boolean hasPermission = serviceProviderRepository.existsByServiceIdAndPermissions_Permission(
                service.getId(), ProviderPermissionEnum.SERVE);
        assertTrue(hasPermission);

        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(service.getId());

        assertNotNull(slots);
        assertEquals(2, slots.size());
        assertEquals(45, Duration.between(slots.get(0).start(), slots.get(0).end()).toMinutes());
        assertEquals(45, Duration.between(slots.get(1).start(), slots.get(1).end()).toMinutes());
    }

    @Test
    void testNoSlotsIfAvailabilityLessThanServiceDuration() {
        Member provider = createAndSaveProvider("noslotscase");
        Service service = createAndSaveServiceWithDuration(provider, 60); // 1h
        createAndSaveServiceProviderWithServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusHours(3);
        Availability av = new Availability();
        //av.setMember(provider);
        av.setStartDateTime(start);
        av.setEndDateTime(start.plusMinutes(40)); // menos do que 1h

        availabilityRepository.save(av);

        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(service.getId());
        assertTrue(slots.isEmpty());
    }

    @Test
    void testSlotsWithOverlapWithAppointmentAreFiltered() {
        mockAuthentication("testUser");

        Member provider = createAndSaveProvider("overlapteste");
        Service service = createAndSaveServiceWithDuration(provider, 30);
        ServiceProvider serviceProvider = createAndSaveServiceProviderWithServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MINUTES);
        Availability av = new Availability();
        //av.setMember(provider);
        av.setStartDateTime(start);
        av.setEndDateTime(start.plusHours(2));
        availabilityRepository.save(av);

        // Create appointment associated with SERVICE PROVIDER
        LocalDateTime appointmentStart = start.plusMinutes(30);
        Appointment appointment = new Appointment();
        appointment.setMember(provider);
        appointment.setServiceProvider(serviceProvider); // Associate with service provider
        appointment.setStartDateTime(appointmentStart);
        appointment.setEndDateTime(appointmentStart.plusMinutes(30));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        // Verify appointment is properly associated
        List<Appointment> providerAppointments = appointmentRepository.findByServiceProviderId(serviceProvider.getId());
        assertEquals(1, providerAppointments.size(), "Appointment should be associated with service provider");

        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(service.getId());

        // Debug: Print all slots
        System.out.println("Generated slots:");
        slots.forEach(slot -> System.out.println(
                slot.start().truncatedTo(ChronoUnit.MINUTES) + " to " +
                        slot.end().truncatedTo(ChronoUnit.MINUTES)
        ));

        // Verify no overlapping slots
        boolean anyOverlap = slots.stream().anyMatch(slot ->
                slot.start().isBefore(appointment.getEndDateTime()) &&
                        slot.end().isAfter(appointment.getStartDateTime())
        );

        assertFalse(anyOverlap, "No slots should overlap with appointment. Found " + slots.size() + " slots.");
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

    public static void mockAuthentication(String username) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Service createAndSaveServiceWithDuration(Member provider, int duration) {
        Service service = new Service();
        service.setName("Serviço Teste");
        service.setDescription("Teste descrição");
        service.setDuration(duration); // minutos
        service.setOwner(provider);
        service.setServiceType(defaultServiceType);
        service.setPrice(20.1);
        service.setDiscount(0);
        service.setNegotiable(false);
        service.setEnabled(true);
        service.setTags(new ArrayList<>());
        return serviceRepository.save(service);
    }

    private ServiceProvider createAndSaveServiceProviderWithServePermission(Member provider, Service service) {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProvider(provider);
        serviceProvider.setService(service);
        serviceProvider.setActive(true);
        serviceProvider = serviceProviderRepository.save(serviceProvider);

        ProviderPermission permission = new ProviderPermission();
        permission.setServiceProvider(serviceProvider);
        permission.setPermission(ProviderPermissionEnum.SERVE);
        providerPermissionRepository.save(permission);

        return serviceProvider; // Return the created service provider
    }
}