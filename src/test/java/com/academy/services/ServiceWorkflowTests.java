package com.academy.services;

import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.dtos.service_type.ServiceTypeResponseDTO;
import com.academy.dtos.tag.TagRequestDTO;
import com.academy.dtos.tag.TagResponseDTO;
import com.academy.exceptions.MemberNotFoundException;
import com.academy.models.Role;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
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

    @BeforeEach
    void setUp() {
        // Set up a Role and Members to be used on all tests
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

        createDummyClient();
        createDummyProvider();

        // Set up a ServiceType and Tags before each test
        createTag();
        createServiceType();
    }

    private ServiceRequestDTO createDTO(List<String> tags) {
        return new ServiceRequestDTO("Test Service", "Test Description", 80, 20, false, 30, "Test Service Type", tags);
    }

    private ServiceProviderRequestDTO createProviderDTO(Long memberId, Long serviceId) {
        return new ServiceProviderRequestDTO(memberId, serviceId, List.of(ProviderPermissionEnum.SERVE), false);
    }

    private void createServiceType() {
        ServiceTypeRequestDTO requestDTO = new ServiceTypeRequestDTO("Test Service Type", "Test Icon.png");
        ServiceTypeResponseDTO responseDTO = serviceTypeService.create(requestDTO);
        serviceTypeService.getServiceTypeEntityById(responseDTO.id());
    }

    private void createTag() {
        TagRequestDTO requestDTO = new TagRequestDTO("tag1", false, List.of());
        TagResponseDTO responseDTO = tagService.create(requestDTO);
        tagService.getTagEntityById(responseDTO.id());
    }

    private AppointmentRequestDTO createAppointmentDTO(Long serviceProviderId) {
        return new AppointmentRequestDTO(serviceProviderId, LocalDateTime.now().plusMinutes(3600), LocalDateTime.now().plusMinutes(3660), 0, "", null);
    }

    private void createDummyClient() {
        Role clientRole = new Role();
        clientRole.setName("CLIENT");
        roleRepository.save(clientRole);

        RegisterRequestDto request = new RegisterRequestDto(
                "client", "Password123!", "client" + "@email.com",
                clientRole.getId(), null, null, null
        );
        memberService.register(request);
    }

    private void createDummyProvider() {
        Role providerRole = new Role();
        providerRole.setName("WORKER");
        roleRepository.save(providerRole);

        RegisterRequestDto request = new RegisterRequestDto(
                "provider", "Password123!", "provider" + "@email.com",
                providerRole.getId(), null, null, null
        );
        memberService.register(request);
    }

    @Test
    void deleteServiceProvider_cascadesProperly() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO(List.of("tag2"));
        ServiceResponseDTO serviceResponseDTO = serviceService.create(serviceRequestDTO);

        Long memberId = memberService.getMemberByUsername("provider").getId();
        Long serviceId = serviceResponseDTO.id();

        ServiceProviderRequestDTO serviceProviderRequestDTO = createProviderDTO(memberId, serviceId);
        serviceProviderService.createServiceProvider(serviceProviderRequestDTO);

        Long serviceProviderId = serviceProviderService.getServiceProviderByUsername("provider").getId();

        AppointmentResponseDTO appointment = appointmentService.createAppointment(
                createAppointmentDTO(serviceProviderId)
        );

        assertThat(appointment).isNotNull();
        assertThat(appointment.serviceProviderId()).isEqualTo(serviceProviderId);

        ServiceProvider serviceProvider = serviceProviderService.getServiceProviderByUsername("provider");
        // Delete the ServiceProvider
        serviceProviderService.deleteServiceProvider(serviceProvider.getId());
        // Verify that the Appointment still exists and the serviceProvider is Inactive
        AppointmentResponseDTO appointmentRspDTO = appointmentService.getAppointmentById(appointment.id());
        assertThat(appointmentRspDTO).isNotNull();
        assertThat(appointmentRspDTO.serviceProviderId()).isGreaterThan(-1);
        assertThat(serviceProviderService.getServiceProviderById(appointmentRspDTO.serviceProviderId())).isNotNull();
        assertFalse(serviceProviderService.getServiceProviderById(appointmentRspDTO.serviceProviderId()).active());
    }

    @Test
    void deleteMember_cascadesProperly() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO(List.of("tag2"));
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
        assertThat(serviceProviderService.getServiceProviderById(ownerServiceProviderId).memberName()).isEqualTo("Deleted User");
        assertThat(appointmentService.getAppointmentById(appointment.id())).isNotNull();
        assertThat(appointmentService.getAppointmentById(appointment.id()).serviceProviderId()).isEqualTo(ownerServiceProviderId);
    }
}
