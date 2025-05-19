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

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
public class ServiceServiceTests {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private TagRepository tagRepository;

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

    // TODO test delete tag with service associated

    @Test
    public void createService_createsTagsAndAssignsServiceType() {
        // Prepare test data
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId()); // Use the ID of the ServiceType
        serviceRequestDTO.setTagNames(List.of("tag1", "tag2"));

        // Call the service layer to create the service
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
        // Create service with duplicate tags
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
        // Create a service using the serviceType and tags
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
        updateRequestDTO.setServiceTypeId(newServiceType.getId()); // New serviceType
        updateRequestDTO.setTagNames(List.of("tag2", "tag3")); // New tags

        ServiceResponseDTO updatedResponse = serviceService.update(createdResponse.getId(), updateRequestDTO);

        // Verify that the service was updated with the new serviceType and tags
        assertThat(updatedResponse.getName()).isEqualTo("Updated Service");
        assertThat(updatedResponse.getServiceTypeName()).isEqualTo(newServiceType.getName());
        assertThat(updatedResponse.getTagNames()).containsExactlyInAnyOrder("tag2", "tag3");
    }

    @Test
    public void deleteService_deletesService() {
        // Create a service
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag1"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        // Delete the service
        serviceService.delete(createdResponse.getId());

        // Verify that the service is deleted
        assertThat(serviceRepository.findById(createdResponse.getId())).isEmpty();
    }

    @Test
    public void deleteService_cascadesCorrectly() {
        // Create a service and associate it with a tag and serviceType
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag1"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        // Check if the tag is correctly associated with the service
        assertThat(tagRepository.findById(tag1.getId()).get().getServices()).isNotEmpty();

        // Delete the service
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
    public void deleteTag_disassociatesTagFromService() {
        // Create a service with tags
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO();
        serviceRequestDTO.setName("Test Service");
        serviceRequestDTO.setDescription("Test Description");
        serviceRequestDTO.setServiceTypeId(serviceType.getId());
        serviceRequestDTO.setTagNames(List.of("tag1", "tag2"));

        ServiceResponseDTO createdResponse = serviceService.create(serviceRequestDTO);

        // Now, delete one tag (tag1)
        tagService.delete(tag1.getId());

        // Ensure that the tag is disassociated from the service
        Service updatedService = serviceRepository.findById(createdResponse.getId()).orElseThrow();
        List<String> remainingTags = updatedService.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        assertThat(remainingTags).doesNotContain("tag1");
        assertThat(remainingTags).contains("tag2");
    }
}
