package com.academy.schedulling;

import com.academy.dtos.SlotDTO;
import com.academy.models.Member;
import com.academy.models.Role;
import com.academy.models.Availability;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.*;
import com.academy.services.SchedulingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    private Role defaultRole;

    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        serviceProviderRepository.deleteAll();
        serviceRepository.deleteAll();
        memberRepository.deleteAll();
        roleRepository.deleteAll();

        // Criar um Role obrigatório para os membros
        defaultRole = new Role();
        defaultRole.setName("USER");
        roleRepository.save(defaultRole);
    }

    @Test
    void testGetFreeSlotsForService() {
        // Criar um membro (prestador) com role
        Member provider = new Member();
        provider.setUsername("provider1");
        provider.setPassword("password");
        provider.setEmail("provider1@example.com");
        provider.setRole(defaultRole);  // ESSENCIAL para não dar erro
        memberRepository.save(provider);

        // Criar um serviço e associar ao provider
        Service service = new Service();
        service.setName("Test Service");
        service.setOwner(provider);
        serviceRepository.save(service);

        // Associar o prestador ao serviço
        ServiceProvider sp = new ServiceProvider();
        sp.setProvider(provider);
        sp.setService(service);
        serviceProviderRepository.save(sp);

        // Criar uma disponibilidade para o provider
        Availability availability = new Availability();
        availability.setMember(provider);
        availability.setStartDateTime(LocalDateTime.now().plusHours(1));
        availability.setEndDateTime(LocalDateTime.now().plusHours(3));
        availabilityRepository.save(availability);

        // Nenhuma marcação criada - todos os slots devem estar livres
        List<SlotDTO> freeSlots = schedulingService.getFreeSlotsForService(service.getId());
        assertNotNull(freeSlots);
        assertFalse(freeSlots.isEmpty());

        // Verificar se os slots têm o ID do provider e o username correto
        for (SlotDTO slot : freeSlots) {
            assertEquals(provider.getId(), slot.getProviderId());
            assertEquals(provider.getUsername(), slot.getProviderName());
            assertTrue(slot.getStart().isAfter(LocalDateTime.now()));
        }
    }
}
