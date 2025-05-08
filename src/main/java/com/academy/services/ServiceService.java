package com.academy.services;

import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.ServiceTypeRepository;
import com.academy.repositories.TagRepository;
import com.academy.models.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceService {

    private ServiceTypeRepository serviceTypeRepository;
    private ServiceRepository serviceRepository;
    private TagRepository tagRepository;

    public ServiceService(ServiceRepository serviceRepository, TagRepository tagRepository, ServiceTypeRepository serviceTypeRepository) {
        this.serviceRepository = serviceRepository;
        this.tagRepository = tagRepository;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    public void createService(String name, String description, double price, int discount, boolean isNegotiable,
                              int duration, Long serviceTypeId, List<Long> tagIds) {

        // Create the Service entity
        Service service = new Service();

        // Set all the fields
        service.setName(name);
        service.setDescription(description);
        service.setPrice(price);
        service.setDiscount(discount);
        service.setNegotiable(isNegotiable);
        service.setDuration(duration);

        // Set the type
        Optional<ServiceType> type = serviceTypeRepository.findById(serviceTypeId);
        type.ifPresent(service::setServiceType);

        // Set the tags
        List<Tag> tags = tagRepository.findAllById(tagIds);
        service.setTags(tags);

        // Set the creation time
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());

        // Save the service
        serviceRepository.save(service);
        System.out.println(service);
    }

    public void deleteService(Service service) {
        serviceRepository.delete(service);
    }

    public void updateService(Service service) {
        serviceRepository.save(service);
    }

    public List<Service> getServices() {
        return serviceRepository.findAll();
    }

    public Service getServiceById(long id) {
        Optional<Service> service = serviceRepository.findById(id);
        return service.orElse(null);
    }
}