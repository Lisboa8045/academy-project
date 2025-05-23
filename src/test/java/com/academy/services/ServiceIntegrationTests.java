package com.academy.services;

import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.exceptions.ServiceNotFoundException;
import com.academy.exceptions.ServiceTypeNotFoundException;
import com.academy.models.Service;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.ServiceTypeRepository;
import com.academy.repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class ServiceIntegrationTests {

    private final ServiceService serviceService;
    private final TagService tagService;
    private final ServiceRepository serviceRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final TagRepository tagRepository;

    @Autowired
    public ServiceIntegrationTests(ServiceService serviceService,
                                   TagService tagService,
                                   ServiceRepository serviceRepository,
                                   ServiceTypeRepository serviceTypeRepository, TagRepository tagRepository
    ) {

        this.serviceService = serviceService;
        this.tagService = tagService;
        this.serviceRepository = serviceRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.tagRepository = tagRepository;
    }

    private ServiceType serviceType;
    private Tag tag1;

    @BeforeEach
    public void setUp() {
        // Set up a ServiceType and Tags before each test
        serviceType = new ServiceType();
        serviceType.setName("Test Service Type");
        serviceType = serviceTypeRepository.save(serviceType);

        tag1 = new Tag();
        tag1.setName("tag1");
        tag1 = tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setName("tag2");
        tagRepository.save(tag2);
    }

    @AfterEach
    void tearDown() {
        serviceRepository.deleteAll();
        tagRepository.deleteAll();
        serviceTypeRepository.deleteAll();
    }

    @Test
    public void updateService_serviceNotFound_throwsException() {
        assertThatThrownBy(() -> serviceService.update(999L, new ServiceRequestDTO()))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    public void deleteService_serviceNotFound_throwsException() {
        assertThatThrownBy(() -> serviceService.delete(999L))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    public void updateService_invalidServiceType_throwsException() {
        ServiceRequestDTO updateRequestDTO = new ServiceRequestDTO();
        updateRequestDTO.setName("Updated Service");
        updateRequestDTO.setDescription("Updated Description");
        updateRequestDTO.setServiceTypeId(999L);
        updateRequestDTO.setTagNames(List.of("tag1"));

        assertThatThrownBy(() -> serviceService.update(1L, updateRequestDTO))
                .isInstanceOf(ServiceTypeNotFoundException.class);
    }

    @Test
    public void deleteTag_notAssociatedWithAnyService_doesNotThrowException() {
        Tag tag3 = new Tag();
        tag3.setName("tag3");
        tagRepository.save(tag3);  // A tag that is not used in any service

        assertThatCode(() -> tagService.delete(tag3.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    public void deleteTag_associatedWithService() {
        Tag tag3 = new Tag();
        tag3.setName("tag3");
        tagRepository.save(tag3);

        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag3"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);

        assertThatCode(() -> tagService.delete(tag3.getId()))
                .doesNotThrowAnyException();

        ServiceResponseDTO updatedResponseDTO = serviceService.getById(responseDTO.getId());

        assertThat(updatedResponseDTO.getTagNames()).isEmpty(); // Ensure that tags are empty
    }

    @Test
    public void createService_createsTagsAndAssignsServiceType() {
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag1", "tag2"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);

        // Verify that the service was created correctly
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getName()).isEqualTo("Test Service");
        assertThat(responseDTO.getDescription()).isEqualTo("Test Description");
        assertThat(responseDTO.getServiceTypeName()).isEqualTo(serviceType.getName());
        assertThat(responseDTO.getTagNames()).containsExactlyInAnyOrder("tag1", "tag2"); // Ensure that tags are associated
    }

    @Test
    public void createService_duplicateTags_doesNotCreateDuplicateTags() {
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag1", "tag1"));

        ServiceResponseDTO responseDTO = serviceService.create(serviceRequestDTO);

        // Verify that only one tag is created
        assertThat(responseDTO.getTagNames()).containsExactlyInAnyOrder("tag1");
    }

    @Test
    public void updateService_updatesServiceTypeAndTags() {
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag1", "tag2"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        // Update the service with a new serviceType and tags
        ServiceType newServiceType = new ServiceType();
        newServiceType.setName("New Service Type");
        newServiceType = serviceTypeRepository.save(newServiceType);

        ServiceRequestDTO updateRequestDTO = new ServiceRequestDTO();
        updateRequestDTO.setName("Updated Service");
        updateRequestDTO.setDescription("Updated Description");
        updateRequestDTO.setServiceTypeId(newServiceType.getId());
        updateRequestDTO.setTagNames(List.of("tag2", "tag3"));

        ServiceResponseDTO updatedResponse = serviceService.update(createdResponse.getId(), updateRequestDTO);

        // Verify that the service was updated with the new serviceType and tags
        assertThat(updatedResponse.getName()).isEqualTo("Updated Service");
        assertThat(updatedResponse.getServiceTypeName()).isEqualTo(newServiceType.getName());
        assertThat(updatedResponse.getTagNames()).containsExactlyInAnyOrder("tag2", "tag3");
    }

    @Test
    public void deleteService_deletesService() {
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag1"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        serviceService.delete(createdResponse.getId());

        // Verify that the service is deleted
        assertThat(serviceRepository.findById(createdResponse.getId())).isEmpty();
    }

    @Test
    public void deleteService_cascadesCorrectly() {
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag1"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        assertThat(tagRepository.findById(tag1.getId()).get().getServices()).isNotEmpty();

        serviceService.delete(createdResponse.getId());

        // Verify that the service is deleted
        assertThat(serviceRepository.findById(createdResponse.getId())).isEmpty();

        // Verify that associated tag is not deleted
        assertThat(tagRepository.findById(tag1.getId())).isNotEmpty();

        // Verify that associated serviceType is not deleted
        assertThat(serviceTypeRepository.findById(serviceType.getId())).isNotEmpty();

        // Verify that tag has no associations after deletion
        assertThat(tagRepository.findById(tag1.getId()).get().getServices()).isEmpty();
    }

    @Test
    public void deleteTag_dissociatesTagFromService() {
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag1", "tag2"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        // Delete one tag (tag1)
        tagService.delete(tag1.getId());

        // Ensure that the tag is dissociated from the service
        Service updatedService = serviceRepository.findById(createdResponse.getId()).orElseThrow();
        List<String> remainingTags = updatedService.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        assertThat(remainingTags).doesNotContain("tag1");
        assertThat(remainingTags).contains("tag2");
    }

    @Test
    public void updateService_updateTags() {
        ServiceRequestDTO createServiceDTO = new ServiceRequestDTO();
        createServiceDTO.setName("Test Service");
        createServiceDTO.setDescription("Test Description");
        createServiceDTO.setServiceTypeId(serviceType.getId());
        createServiceDTO.setTagNames(List.of("tag1", "tag2"));

        ServiceResponseDTO createResponseDTO = serviceService.create(createServiceDTO);

        // Verify initial tags
        assertThat(createResponseDTO.getTagNames()).containsExactlyInAnyOrder("tag1", "tag2");

        // Verify that the tags have the service in their 'services' list
        Tag tag1 = tagRepository.findByName("tag1").orElseThrow();
        Tag tag2 = tagRepository.findByName("tag2").orElseThrow();
        assertThat(tag1.getServices()).contains(serviceRepository.findById(createResponseDTO.getId()).get());
        assertThat(tag2.getServices()).contains(serviceRepository.findById(createResponseDTO.getId()).get());

        // Prepare new tag list for update (removes "tag2" and adds "tag3" and "tag4")
        ServiceRequestDTO updateServiceDTO = new ServiceRequestDTO();
        updateServiceDTO.setName("Test Service Updated");
        updateServiceDTO.setDescription("Test Description Updated");
        updateServiceDTO.setServiceTypeId(serviceType.getId());
        updateServiceDTO.setTagNames(List.of("tag1", "tag3", "tag4"));

        ServiceResponseDTO updateResponseDTO = serviceService.update(createResponseDTO.getId(), updateServiceDTO);

        // Verify that the service was updated correctly
        assertThat(updateResponseDTO).isNotNull();
        assertThat(updateResponseDTO.getName()).isEqualTo("Test Service Updated");
        assertThat(updateResponseDTO.getDescription()).isEqualTo("Test Description Updated");
        assertThat(updateResponseDTO.getServiceTypeName()).isEqualTo(serviceType.getName());

        // Verify that the tags were updated
        assertThat(updateResponseDTO.getTagNames()).containsExactlyInAnyOrder("tag1", "tag3", "tag4");
        assertThat(updateResponseDTO.getTagNames()).doesNotContain("tag2");

        // Verify that tag2 no longer has the service in their 'services' list
        tag2 = tagRepository.findByName("tag2").orElseThrow();
        assertThat(tag2.getServices()).doesNotContain(serviceRepository.findById(createResponseDTO.getId()).get());

        // Verify that tag1, tag3 and tag4 now have the service in their 'services' list
        tag1 = tagRepository.findByName("tag1").orElseThrow();
        Tag tag3 = tagRepository.findByName("tag3").orElseThrow();
        Tag tag4 = tagRepository.findByName("tag4").orElseThrow();
        assertThat(tag1.getServices()).contains(serviceRepository.findById(createResponseDTO.getId()).get());
        assertThat(tag3.getServices()).contains(serviceRepository.findById(createResponseDTO.getId()).get());
        assertThat(tag4.getServices()).contains(serviceRepository.findById(createResponseDTO.getId()).get());
    }

    @Test
    public void deleteService_removesTagAssociations() {
        ServiceRequestDTO requestDTO = new ServiceRequestDTO();
        requestDTO.setName("Test Deletion Service");
        requestDTO.setDescription("To be deleted");
        requestDTO.setServiceTypeId(serviceType.getId());
        requestDTO.setTagNames(List.of("delete-tag1", "delete-tag2"));

        ServiceResponseDTO responseDTO = serviceService.create(requestDTO);
        Long serviceId = responseDTO.getId();

        // Verify tag-service association exists
        Tag tag1 = tagRepository.findByName("delete-tag1").orElseThrow();
        Tag tag2 = tagRepository.findByName("delete-tag2").orElseThrow();
        assertThat(tag1.getServices()).extracting("id").contains(serviceId);
        assertThat(tag2.getServices()).extracting("id").contains(serviceId);

        serviceService.delete(serviceId);

        // Verify that service is deleted
        assertThat(serviceRepository.findById(serviceId)).isEmpty();

        // Verify that tags no longer reference the deleted service
        tag1 = tagRepository.findByName("delete-tag1").orElseThrow();
        tag2 = tagRepository.findByName("delete-tag2").orElseThrow();
        assertThat(tag1.getServices()).extracting("id").doesNotContain(serviceId);
        assertThat(tag2.getServices()).extracting("id").doesNotContain(serviceId);
    }
}
