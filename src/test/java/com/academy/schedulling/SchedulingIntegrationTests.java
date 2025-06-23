/*
package com.academy.schedulling;

import com.academy.dtos.SlotDTO;
import com.academy.models.member.Member;
import com.academy.models.Role;
import com.academy.models.Availability;
import com.academy.models.ServiceType;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.*;
import com.academy.services.SchedulingService;
import com.academy.repositories.ServiceProviderRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private RoleRepository roleRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceTypeRepository  serviceTypeRepository;

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
    }


        @AfterEach
        void teardown() {
            appointmentRepository.deleteAll();
            availabilityRepository.deleteAll();
            serviceProviderRepository.deleteAll();
            serviceRepository.deleteAll();
            serviceTypeRepository.deleteAll();
            memberRepository.deleteAll();
            roleRepository.deleteAll();
        }

    // Teste principal
    @Test
    void testGetFreeSlotsForService() {
        Member provider = createAndSaveProvider("provider1");
        Service service = createAndSaveService(provider);
        createAndSaveServiceProvider(provider, service);

        Availability availability = new Availability();
        availability.setMember(provider);
        availability.setStartDateTime(LocalDateTime.now().plusHours(1));
        availability.setEndDateTime(LocalDateTime.now().plusHours(3));
        availabilityRepository.save(availability);

        List<SlotDTO> freeSlots = schedulingService.getFreeSlotsForService(service.getId());

        assertNotNull(freeSlots);
        assertFalse(freeSlots.isEmpty());

        for (SlotDTO slot : freeSlots) {
            assertEquals(provider.getId(), slot.getProviderId());
            assertEquals(provider.getUsername(), slot.getProviderName());
            assertTrue(slot.getStart().isAfter(LocalDateTime.now()));
        }
    }

    @Test
    void testAvailabilityShorterThanSlotDuration() {
        Member provider = createAndSaveProvider("shortSlotTest");
        Service service = createAndSaveService(provider);
        createAndSaveServiceProvider(provider, service);

        Availability availability = new Availability();
        availability.setMember(provider);
        availability.setStartDateTime(LocalDateTime.now().plusHours(1));
        availability.setEndDateTime(LocalDateTime.now().plusMinutes(30));
        availabilityRepository.save(availability);

        List<SlotDTO> freeSlots = schedulingService.getFreeSlotsForService(service.getId());

        assertNotNull(freeSlots);
        assertTrue(freeSlots.isEmpty());
    }

    @Test
    void testAvailabilityNotMultipleOfSlotDuration() {
        Member provider = createAndSaveProvider("nonMultipleSlotTest");
        Service service = createAndSaveService(provider);
        createAndSaveServiceProvider(provider, service);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        Availability availability = new Availability();
        availability.setMember(provider);
        availability.setStartDateTime(start);
        availability.setEndDateTime(start.plusMinutes(105)); // 1h45m
        availabilityRepository.save(availability);

        List<SlotDTO> freeSlots = schedulingService.getFreeSlotsForService(service.getId());

        assertNotNull(freeSlots);
        assertEquals(1, freeSlots.size());
        assertEquals(start, freeSlots.get(0).getStart());
        assertEquals(start.plusHours(1), freeSlots.get(0).getEnd());
    }

    @Test
    void testMultipleIrregularAvailabilities() {
        Member provider = createAndSaveProvider("irregularSlotsTest");
        Service service = createAndSaveService(provider);
        createAndSaveServiceProvider(provider, service);

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0).plusHours(1);

        Availability a1 = new Availability();
        a1.setMember(provider);
        a1.setStartDateTime(now.withHour(14).withMinute(15));
        a1.setEndDateTime(now.withHour(15).withMinute(45));
        availabilityRepository.save(a1);

        Availability a2 = new Availability();
        a2.setMember(provider);
        a2.setStartDateTime(now.withHour(16).withMinute(0));
        a2.setEndDateTime(now.withHour(17).withMinute(59));
        availabilityRepository.save(a2);

        List<SlotDTO> freeSlots = schedulingService.getFreeSlotsForService(service.getId());

        assertNotNull(freeSlots);
        assertEquals(2, freeSlots.size());

        assertEquals(a1.getStartDateTime(), freeSlots.get(0).getStart());
        assertEquals(a1.getStartDateTime().plusHours(1), freeSlots.get(0).getEnd());

        assertEquals(a2.getStartDateTime(), freeSlots.get(1).getStart());
        assertEquals(a2.getStartDateTime().plusHours(1), freeSlots.get(1).getEnd());
    }

    // Métodos auxiliares
    private Member createAndSaveProvider(String username) {
        Member provider = new Member();
        provider.setUsername(username);
        provider.setEmail(username + "@example.com");
        provider.setPassword("password");
        provider.setRole(defaultRole);
        return memberRepository.save(provider);
    }

    private Service createAndSaveService(Member provider) {
        Service service = new Service();
        service.setName("Consulta Geral");
        service.setDescription("Consulta médica geral.");
        service.setDuration(30); // int, em minutos
        service.setPrice(50.0);  // double
        service.setDiscount(0);
        service.setNegotiable(false);
        service.setOwner(provider);
        service.setServiceType(defaultServiceType); // Usar o default

        return serviceRepository.save(service);
    }



    private void createAndSaveServiceProvider(Member provider, Service service) {
        ServiceProvider sp = new ServiceProvider();
        sp.setProvider(provider);
        sp.setService(service);
        serviceProviderRepository.save(sp);
    }
}
*/
