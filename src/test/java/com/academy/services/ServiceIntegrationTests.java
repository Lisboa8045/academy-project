package com.academy.services;

import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Member;
import com.academy.models.Role;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.models.service.Service;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.ProviderPermissionRepository;
import com.academy.repositories.RoleRepository;
import com.academy.repositories.ServiceProviderRepository;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.ServiceTypeRepository;
import com.academy.repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
@ExtendWith(SpringExtension.class)
@WithMockUser(username = "owner")
public class ServiceIntegrationTests {
    private final ServiceService serviceService;
    private final TagService tagService;
    private final ServiceRepository serviceRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final TagRepository tagRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public ServiceIntegrationTests(ServiceService serviceService,
                                   TagService tagService,
                                   ServiceRepository serviceRepository,
                                   ServiceTypeRepository serviceTypeRepository,
                                   TagRepository tagRepository,
                                   MemberRepository memberRepository,
                                   RoleRepository roleRepository,
                                   ServiceProviderRepository serviceProviderRepository,
                                   ProviderPermissionRepository providerPermissionRepository
    ) {

        this.serviceService = serviceService;
        this.tagService = tagService;
        this.serviceRepository = serviceRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.tagRepository = tagRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
    }

    private ServiceType defaultServiceType;
    private Tag defaultTag;

    @BeforeEach
    void setUp() {
        // Set up a ServiceType, Member and Tags before each test
        Role role = new Role();
        role.setId(1);
        role.setName("ADMIN");
        roleRepository.save(role);

        Member member = new Member();
        member.setUsername("owner");
        member.setPassword("password");
        member.setEmail("owner@email.com");
        member.setRole(role);
        memberRepository.save(member);

        defaultTag = createTag("tag1");
        defaultServiceType = createServiceType("Test Service Type");
    }

    private ServiceRequestDTO createDTO(String name, String description, String serviceTypeName, List<String> tags) {
        return new ServiceRequestDTO(name, description, 80, 20, false, 30, serviceTypeName, tags);
    }

    private ServiceType createServiceType(String name) {
        ServiceType serviceType = new ServiceType();
        serviceType.setName(name);
        serviceType.setIcon("Test Icon.png");
        serviceType = serviceTypeRepository.save(serviceType);
        return serviceType;
    }

    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setCustom(false);
        tag = tagRepository.save(tag);
        return tag;
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
    void updateService_invalidServiceType_throwsException() {
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
    void deleteTag_associatedWithService() {
        Tag tag2 = createTag("tag2");
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag2"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);

        assertThatCode(() -> tagService.delete(tag2.getId()))
                .doesNotThrowAnyException();

        ServiceResponseDTO updatedResponseDTO = serviceService.getById(responseDTO.id());

        assertThat(updatedResponseDTO.tagNames()).isEmpty(); // Ensure that tags are empty
    }

    @Test
    void createService_createsTagsAndAssignsServiceType() {
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
    void createService_duplicateTags_doesNotCreateDuplicateTags() {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1", "tag1"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);

        // Verify that only one tag is created
        assertThat(responseDTO.tagNames()).containsExactlyInAnyOrder("tag1");
    }

    @Test
    void updateService_updatesServiceTypeAndTags() {
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
        assertThat(serviceTypeRepository.findById(defaultServiceType.getId()).get().getServices()).isEmpty();
    }

    @Test
    void deleteService_deletesService() {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        serviceService.delete(createdResponse.id());

        // Verify that the service is deleted
        assertThat(serviceRepository.findById(createdResponse.id())).isEmpty();
    }

    @Test
    void deleteService_cascadesCorrectly() {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        assertThat(tagRepository.findById(defaultTag.getId()).get().getServices()).isNotEmpty();

        serviceService.delete(createdResponse.id());

        // Verify that the service is deleted
        assertThat(serviceRepository.findById(createdResponse.id())).isEmpty();

        // Verify that associated tag is not deleted
        assertThat(tagRepository.findById(defaultTag.getId())).isNotEmpty();

        // Verify that associated serviceType is not deleted
        assertThat(serviceTypeRepository.findById(defaultServiceType.getId())).isNotEmpty();

        // Verify that tag has no associations after deletion
        assertThat(tagRepository.findById(defaultTag.getId()).get().getServices()).isEmpty();
    }

    @Test
    void deleteTag_dissociatesTagFromService() {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1", "tag2"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        // Delete one tag (tag1)
        tagService.delete(defaultTag.getId());

        // Ensure that the tag is dissociated from the service
        Service updatedService = serviceRepository.findById(createdResponse.id()).orElseThrow();
        List<String> remainingTags = updatedService.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        assertThat(remainingTags).doesNotContain("tag1");
        assertThat(remainingTags).contains("tag2");
    }

    @Test
    void updateService_updateTags() {
        ServiceRequestDTO createServiceDTO = createDTO("Test Service", "Test Description", "Test Service Type", List.of("tag1", "tag2"));

        ServiceResponseDTO createResponseDTO = serviceService.create(createServiceDTO);

        // Verify initial tags
        assertThat(createResponseDTO.tagNames()).containsExactlyInAnyOrder("tag1", "tag2");

        // Verify that the tags have the service in their 'services' list
        Tag tag1 = tagRepository.findByName("tag1").orElseThrow();
        Tag tag2 = tagRepository.findByName("tag2").orElseThrow();
        assertThat(tag1.getServices()).contains(serviceRepository.findById(createResponseDTO.id()).get());
        assertThat(tag2.getServices()).contains(serviceRepository.findById(createResponseDTO.id()).get());

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
        tag2 = tagRepository.findByName("tag2").orElseThrow();
        assertThat(tag2.getServices()).doesNotContain(serviceRepository.findById(createResponseDTO.id()).get());

        // Verify that tag1, tag3 and tag4 now have the service in their 'services' list
        tag1 = tagRepository.findByName("tag1").orElseThrow();
        Tag tag3 = tagRepository.findByName("tag3").orElseThrow();
        Tag tag4 = tagRepository.findByName("tag4").orElseThrow();
        assertThat(tag1.getServices()).contains(serviceRepository.findById(createResponseDTO.id()).get());
        assertThat(tag3.getServices()).contains(serviceRepository.findById(createResponseDTO.id()).get());
        assertThat(tag4.getServices()).contains(serviceRepository.findById(createResponseDTO.id()).get());
    }

    @Test
    void deleteService_removesTagAssociations() {
        ServiceRequestDTO serviceRequestDTO = createDTO("Test Deletion Service", "To be deleted", "Test Service Type", List.of("delete-tag1", "delete-tag2"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);
        Long serviceId = responseDTO.id();

        // Verify tag-service association exists
        Tag tag2 = tagRepository.findByName("delete-tag1").orElseThrow();
        Tag tag3 = tagRepository.findByName("delete-tag2").orElseThrow();
        assertThat(tag2.getServices()).extracting("id").contains(serviceId);
        assertThat(tag3.getServices()).extracting("id").contains(serviceId);

        serviceService.delete(serviceId);

        // Verify that service is deleted
        assertThat(serviceRepository.findById(serviceId)).isEmpty();

        serviceTypeRepository.findAll();

        // Verify that tags no longer reference the deleted service
        tag2 = tagRepository.findByName("delete-tag1").orElseThrow();
        tag3 = tagRepository.findByName("delete-tag2").orElseThrow();
        assertThat(tag2.getServices()).extracting("id").doesNotContain(serviceId);
        assertThat(tag3.getServices()).extracting("id").doesNotContain(serviceId);
    }
}
