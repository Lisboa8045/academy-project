package com.academy.services;

import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.dtos.service_type.ServiceTypeResponseDTO;
import com.academy.dtos.tag.TagRequestDTO;
import com.academy.dtos.tag.TagResponseDTO;
import com.academy.exceptions.MemberNotFoundException;
import com.academy.models.Role;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.RoleRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@WithMockUser(username = "owner")
class ServiceWorkflowTests {

    private final MemberService memberService;
    private final ServiceService serviceService;
    private final ServiceProviderService serviceProviderService;
    private final AppointmentService appointmentService;
    private final RoleRepository roleRepository;
    private final ServiceTypeService serviceTypeService;
    private final TagService tagService;

    @Autowired
    public ServiceWorkflowTests(MemberService memberService,
                                ServiceService serviceService,
                                ServiceProviderService serviceProviderService,
                                AppointmentService appointmentService,
                                RoleRepository roleRepository, ServiceTypeService serviceTypeService, TagService tagService
    ) {
        this.memberService = memberService;
        this.serviceService = serviceService;
        this.serviceProviderService = serviceProviderService;
        this.appointmentService = appointmentService;
        this.roleRepository = roleRepository;
        this.serviceTypeService = serviceTypeService;
        this.tagService = tagService;
    }

    private ServiceType defaultServiceType;
    private Tag defaultTag;
    private Long clientId;

    @BeforeEach
    void setUp() {
        // Set up a Role and Member to be used on all tests
        Role role = new Role();
        role.setName("ADMIN");
        roleRepository.save(role);

        try {
            memberService.getMemberByUsername("owner");
        }
        catch (MemberNotFoundException e) {
            RegisterRequestDto requestDTO = new RegisterRequestDto("owner", "Password123!", "owner@email.com",
                    role.getId(), null, null, null);
            memberService.register(requestDTO);
        }

        createDummyClient("client");
        clientId = memberService.getMemberByUsername("client").getId();

        // Set up a ServiceType and Tags before each test
        defaultTag = createTag("tag1");
        defaultServiceType = createServiceType("Test Service Type");
    }

    private ServiceRequestDTO createDTO(String name, String description, String serviceTypeName, List<String> tags) {
        return new ServiceRequestDTO(name, description, 80, 20, false, 30, serviceTypeName, tags);
    }

    private ServiceType createServiceType(String name) {
        ServiceTypeRequestDTO requestDTO = new ServiceTypeRequestDTO(name, "Test Icon.png");
        ServiceTypeResponseDTO responseDTO = serviceTypeService.create(requestDTO);
        return serviceTypeService.getServiceTypeEntityById(responseDTO.id());
    }

    private Tag createTag(String name) {
        TagRequestDTO requestDTO = new TagRequestDTO(name, false, List.of());
        TagResponseDTO responseDTO = tagService.create(requestDTO);
        return tagService.getTagEntityById(responseDTO.id());
    }

    private AppointmentRequestDTO createAppointmentDTO(Long serviceProviderId) {
        return new AppointmentRequestDTO(serviceProviderId, LocalDateTime.now().plusMinutes(3600), null, 0, "", null);
    }

    private void createDummyClient(String username) {
        Role clientRole = new Role();
        clientRole.setName("CLIENT");
        roleRepository.save(clientRole);

        RegisterRequestDto request = new RegisterRequestDto(
                username, "Password123!", username + "@email.com",
                clientRole.getId(), null, null, null
        );
        memberService.register(request);
    }

    @Test
    void deleteServiceProvider_cascadesProperly() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag2"));
        serviceService.create(serviceRequestDTO);

        Long serviceProviderId = serviceProviderService.getServiceProviderByUsername("owner").getId();

        AppointmentResponseDTO appointment = appointmentService.createAppointment(
                createAppointmentDTO(serviceProviderId)
        );

        assertThat(appointment).isNotNull();
        assertThat(appointment.serviceProviderId()).isEqualTo(serviceProviderId);

        ServiceProvider ownerServiceProvider = serviceProviderService.getServiceProviderByUsername("owner");
        // Delete the ServiceProvider
        serviceProviderService.deleteServiceProvider(ownerServiceProvider.getId());
        // Verify that the Appointment still exists and the serviceProvider is Inactive
        AppointmentResponseDTO appointmentRspDTO = appointmentService.getAppointmentById(appointment.id());
        assertThat(appointmentRspDTO).isNotNull();
        assertThat(appointmentRspDTO.serviceProviderId()).isGreaterThan(-1);
        assertThat(serviceProviderService.getServiceProviderById(appointmentRspDTO.serviceProviderId())).isNotNull();
        assertFalse(serviceProviderService.getServiceProviderById(appointmentRspDTO.serviceProviderId()).get().active());
    }

    @Test
    void deleteMember_cascadesProperly() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag2"));
        serviceService.create(serviceRequestDTO);

        Long serviceProviderId = serviceProviderService.getServiceProviderByUsername("owner").getId();

        AppointmentResponseDTO appointment = appointmentService.createAppointment(
                createAppointmentDTO(serviceProviderId)
        );

        assertThat(appointment).isNotNull();
        assertThat(appointment.serviceProviderId()).isEqualTo(serviceProviderId);
        Long ownerServiceProviderId = serviceProviderService.getServiceProviderByUsername("owner").getId();

        memberService.deleteMember(memberService.getMemberByUsername("owner").getId());
        // Verify that the ServiceProvider still exists but with no member link
        assertThat(serviceProviderService.getServiceProviderById(ownerServiceProviderId)).isNotNull();
        assertThat(serviceProviderService.getServiceProviderById(ownerServiceProviderId).get().memberName()).isEqualTo("Deleted User");
        assertThat(appointmentService.getAppointmentById(appointment.id())).isNotNull();
        assertThat(appointmentService.getAppointmentById(appointment.id()).serviceProviderId()).isEqualTo(ownerServiceProviderId);
    }
}
