package com.academy.services;

import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.dtos.service_type.ServiceTypeResponseDTO;
import com.academy.dtos.tag.TagRequestDTO;
import com.academy.dtos.tag.TagResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.MemberNotFoundException;
import com.academy.models.Role;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.models.service.Service;
import com.academy.repositories.RoleRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@WithMockUser(username = "owner")
public class ServiceIntegrationTests {
    private final ServiceService serviceService;
    private final TagService tagService;
    private final ServiceTypeService serviceTypeService;
    private final RoleRepository roleRepository; // we do not have a RoleService
    private final MemberService memberService;

    @Autowired
    public ServiceIntegrationTests(ServiceService serviceService,
                                   TagService tagService,
                                   ServiceTypeService serviceTypeService,
                                   MemberService memberService,
                                   RoleRepository roleRepository
    ) {

        this.serviceService = serviceService;
        this.tagService = tagService;
        this.serviceTypeService = serviceTypeService;
        this.memberService = memberService;
        this.roleRepository = roleRepository;
    }

    private ServiceType defaultServiceType;
    private Tag defaultTag;

    @BeforeEach
    void setUp() {
        // Set up a ServiceType and Tags before each test

        defaultTag = createTag("tag1");
        defaultServiceType = createServiceType("Test Service Type");
    }

    @BeforeAll
    void setUpOnce() {
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

    @Test
    void updateService_serviceNotFound_throwsException() {
        assertThatThrownBy(() -> serviceService.update(999L, createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1"))))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteService_serviceNotFound_throwsException() {
        assertThatThrownBy(() -> serviceService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateService_invalidServiceType_throwsException() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        ServiceRequestDTO updateRequestDTO = createDTO("Updated Service", "Updated Description", "Non-Existing Type", List.of("tag1"));

        assertThatThrownBy(() -> serviceService.update(createdResponse.id(), updateRequestDTO))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteTag_notAssociatedWithAnyService_doesNotThrowException() {
        Tag tag2 = createTag("tag2");

        assertThatCode(() -> tagService.delete(tag2.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void deleteTag_associatedWithService() throws BadRequestException {
        Tag tag2 = createTag("tag2");
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag2"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);

        assertThatCode(() -> tagService.delete(tag2.getId()))
                .doesNotThrowAnyException();

        ServiceResponseDTO updatedResponseDTO = serviceService.getById(responseDTO.id());

        assertThat(updatedResponseDTO.tagNames()).isEmpty(); // Ensure that tags are empty
    }

    @Test
    void createService_createsTagsAndAssignsServiceType() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1", "tag2"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);

        // Verify that the service was created correctly
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.name()).isEqualTo("Test Service");
        assertThat(responseDTO.description()).isEqualTo("Test Description");
        assertThat(responseDTO.serviceTypeName()).isEqualTo(defaultServiceType.getName());
        assertThat(responseDTO.tagNames()).containsExactlyInAnyOrder("tag1", "tag2"); // Ensure that tags are associated
    }

    @Test
    void createService_duplicateTags_doesNotCreateDuplicateTags() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1", "tag1"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);

        // Verify that only one tag is created
        assertThat(responseDTO.tagNames()).containsExactlyInAnyOrder("tag1");
    }

    @Test
    void updateService_updatesServiceTypeAndTags() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1", "tag2"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        // Update the service with a new serviceType and tags
        ServiceType newServiceType = createServiceType("New Service Type");

        ServiceRequestDTO updateRequestDTO = createDTO("Updated Service", "Updated Description", "New Service Type", List.of("tag2", "tag3"));

        ServiceResponseDTO updatedResponse = serviceService.update(createdResponse.id(), updateRequestDTO);

        // Verify that the service was updated with the new serviceType and tags
        assertThat(updatedResponse.name()).isEqualTo("Updated Service");
        assertThat(updatedResponse.serviceTypeName()).isEqualTo(newServiceType.getName());
        assertThat(updatedResponse.tagNames()).containsExactlyInAnyOrder("tag2", "tag3");

        // Verify that the old serviceType does not contain a reference to the service
        assertThat(serviceTypeService.getServiceTypeEntityById(defaultServiceType.getId()).getServices()).isEmpty();
    }

    @Test
    void deleteService_deletesService() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        serviceService.delete(createdResponse.id());

        // Verify that the service is deleted
        assertThatThrownBy(() -> serviceService.getServiceEntityById(createdResponse.id()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteService_cascadesCorrectly() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        assertThat(tagService.getTagEntityById(defaultTag.getId()).getServices()).isNotEmpty();

        serviceService.delete(createdResponse.id());

        // Verify that the service is deleted
        assertThatThrownBy(() -> serviceService.getServiceEntityById(createdResponse.id()))
                .isInstanceOf(EntityNotFoundException.class);

        // Verify that associated tag is not deleted
        assertThatCode(() -> tagService.getTagEntityById(defaultTag.getId()))
                .doesNotThrowAnyException();

        // Verify that associated serviceType is not deleted
        assertThatCode(() -> serviceTypeService.getServiceTypeEntityById(defaultServiceType.getId()))
                .doesNotThrowAnyException();

        // Verify that tag has no associations after deletion
        assertThat(tagService.getTagEntityById(defaultTag.getId()).getServices()).isEmpty();
    }

    @Test
    void deleteTag_dissociatesTagFromService() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1", "tag2"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        // Delete one tag (tag1)
        tagService.delete(defaultTag.getId());

        // Ensure that the tag is dissociated from the service
        Service updatedService = serviceService.getServiceEntityById(createdResponse.id());
        List<String> remainingTags = updatedService.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        assertThat(remainingTags).doesNotContain("tag1");
        assertThat(remainingTags).contains("tag2");
    }

    @Test
    void updateService_updateTags() throws BadRequestException {
        ServiceRequestDTO createServiceDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1", "tag2"));

        ServiceResponseDTO createResponseDTO = serviceService.create(createServiceDTO);

        // Verify initial tags
        assertThat(createResponseDTO.tagNames()).containsExactlyInAnyOrder("tag1", "tag2");

        // Verify that the tags have the service in their 'services' list
        Tag tag1 = tagService.getTagEntityByName("tag1");
        Tag tag2 = tagService.getTagEntityByName("tag2");
        assertThat(tag1.getServices()).contains(serviceService.getServiceEntityById(createResponseDTO.id()));
        assertThat(tag2.getServices()).contains(serviceService.getServiceEntityById(createResponseDTO.id()));

        // Prepare new tag list for update (removes "tag2" and adds "tag3" and "tag4")
        ServiceRequestDTO updateServiceDTO = createDTO("Updated Service", "Updated Description", "Test Service Type", List.of("tag1", "tag3", "tag4"));

        ServiceResponseDTO updateResponseDTO = serviceService.update(createResponseDTO.id(), updateServiceDTO);

        // Verify that the service was updated correctly
        assertThat(updateResponseDTO).isNotNull();
        assertThat(updateResponseDTO.name()).isEqualTo("Updated Service");
        assertThat(updateResponseDTO.description()).isEqualTo("Updated Description");
        assertThat(updateResponseDTO.serviceTypeName()).isEqualTo(defaultServiceType.getName());

        // Verify that the tags were updated
        assertThat(updateResponseDTO.tagNames()).containsExactlyInAnyOrder("tag1", "tag3", "tag4");
        assertThat(updateResponseDTO.tagNames()).doesNotContain("tag2");

        // Verify that tag2 no longer has the service in their 'services' list
        tag2 = tagService.getTagEntityByName("tag2");
        assertThat(tag2.getServices()).doesNotContain(serviceService.getServiceEntityById(createResponseDTO.id()));

        // Verify that tag1, tag3 and tag4 now have the service in their 'services' list
        tag1 = tagService.getTagEntityByName("tag1");
        Tag tag3 = tagService.getTagEntityByName("tag3");
        Tag tag4 = tagService.getTagEntityByName("tag4");
        assertThat(tag1.getServices()).contains(serviceService.getServiceEntityById(createResponseDTO.id()));
        assertThat(tag3.getServices()).contains(serviceService.getServiceEntityById(createResponseDTO.id()));
        assertThat(tag4.getServices()).contains(serviceService.getServiceEntityById(createResponseDTO.id()));
    }

    @Test
    void deleteService_removesTagAssociations() throws BadRequestException {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Deletion Service", "To be deleted", "Test Service Type", List.of("delete-tag1", "delete-tag2"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);
        Long serviceId = responseDTO.id();

        // Verify tag-service association exists
        Tag tag2 = tagService.getTagEntityByName("delete-tag1");
        Tag tag3 = tagService.getTagEntityByName("delete-tag2");
        assertThat(tag2.getServices()).extracting("id").contains(serviceId);
        assertThat(tag3.getServices()).extracting("id").contains(serviceId);

        serviceService.delete(serviceId);

        // Verify that service is deleted
        assertThatThrownBy(() -> serviceService.getServiceEntityById(serviceId))
                .isInstanceOf(EntityNotFoundException.class);

        // Verify that tags no longer reference the deleted service
        tag2 = tagService.getTagEntityByName("delete-tag1");
        tag3 = tagService.getTagEntityByName("delete-tag2");
        assertThat(tag2.getServices()).extracting("id").doesNotContain(serviceId);
        assertThat(tag3.getServices()).extracting("id").doesNotContain(serviceId);
    }
}
