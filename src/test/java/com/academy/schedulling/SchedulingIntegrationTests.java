package com.academy.schedulling;

import com.academy.dtos.SlotDTO;
import com.academy.models.appointment.Appointment;
import com.academy.models.member.Member;
import com.academy.models.member.MemberStatusEnum;
import com.academy.models.Role;
import com.academy.models.Availability;
import com.academy.models.ServiceType;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.*;
import com.academy.services.SchedulingService;
import com.academy.repositories.ServiceProviderRepository;

import org.junit.jupiter.api.AfterEach;
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
        // Limpeza defensiva no início
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        serviceProviderRepository.deleteAll();
        serviceRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        memberRepository.deleteAll();
        roleRepository.deleteAll();

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

        var authentication = new UsernamePasswordAuthenticationToken("teste1", "Teste123.", List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();

        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
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
    void testSlotDurationMatchServiceDuration() {

        mockAuthentication("testUser");

        Member provider = createAndSaveProvider("slotmatch");
        Service service = createAndSaveServiceWithDuration(provider, 45); // 45 minutos
        createAndSaveServiceProviderWithServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        Availability av = new Availability();
        av.setMember(provider);
        av.setStartDateTime(start);
        av.setEndDateTime(start.plusMinutes(90)); // 1h30

        availabilityRepository.save(av);

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
        av.setMember(provider);
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
        Service service = createAndSaveServiceWithDuration(provider, 30); // 30min
        createAndSaveServiceProviderWithServePermission(provider, service);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        Availability av = new Availability();
        av.setMember(provider);
        av.setStartDateTime(start);
        av.setEndDateTime(start.plusHours(2)); // 2h janela

        availabilityRepository.save(av);

        Appointment appointment = new Appointment();
        appointment.setMember(provider);
        appointment.setServiceProvider(serviceProviderRepository.findAll().get(0));
        appointment.setStartDateTime(start.plusMinutes(30));
        appointment.setEndDateTime(start.plusMinutes(60));
        appointmentRepository.save(appointment);

        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(service.getId());
        assertFalse(slots.stream().anyMatch(slot -> slot.start().equals(start.plusMinutes(30))));
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
        service.setTags(new ArrayList<>());
        return serviceRepository.save(service);
    }

    private void createAndSaveServiceProviderWithServePermission(Member provider, Service service) {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProvider(provider);
        serviceProvider.setService(service);

        ProviderPermission permission = new ProviderPermission();
        permission.setServiceProvider(serviceProvider);
        permission.setPermission(ProviderPermissionEnum.SERVE);

        serviceProvider.setPermissions(List.of(permission));
        serviceProviderRepository.save(serviceProvider);
        // salve permission se necessário, dependendo do design do seu repositório
    }

}