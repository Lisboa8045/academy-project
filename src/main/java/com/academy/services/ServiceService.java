package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.service.ServiceMapper;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Member;
import com.academy.models.Tag;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.ServiceTypeRepository;
import com.academy.repositories.TagRepository;
import com.academy.specifications.ServiceSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    @Lazy
    private final ServiceProviderService serviceProviderService;
    private final ServiceRepository serviceRepository;
    private final TagRepository tagRepository;
    private final ServiceMapper serviceMapper;
    private final AuthenticationFacade authenticationFacade;
    private final MemberService memberService;
    private final TagService tagService;
    private final ServiceTypeRepository serviceTypeRepository;
    public ServiceService(ServiceRepository serviceRepository,
                          TagRepository tagRepository,
                          ServiceMapper serviceMapper,
                          @Lazy ServiceProviderService serviceProviderService,
                          AuthenticationFacade authenticationFacade,
                          MemberService memberService,
                          TagService tagService,
                          ServiceTypeRepository serviceTypeRepository) {
        this.serviceRepository = serviceRepository;
        this.tagRepository = tagRepository;
        this.serviceMapper = serviceMapper;
        this.serviceProviderService = serviceProviderService;
        this.authenticationFacade = authenticationFacade;
        this.memberService = memberService;
        this.tagService = tagService;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    // Create
    @Transactional
    public ServiceResponseDTO create(ServiceRequestDTO dto) {
        Member member = memberService.getMemberByUsername(authenticationFacade.getUsername());
        Service service = serviceMapper.toEntity(dto, member.getId());

        List<Tag> tags = tagService.findOrCreateTagsByNames(dto.tagNames());
        linkServiceToTags(service, tags); // Set up the bidirectional link

        Service savedService = serviceRepository.save(service);

        createOwnerServiceProvider(new ServiceProviderRequestDTO(
                member.getId(),
                savedService.getId(),
                Arrays.asList(ProviderPermissionEnum.values())
        ));
        return serviceMapper.toDto(savedService, getPermissionsByProviderUsernameAndServiceId(member.getUsername(), savedService.getId()));
    }
    private ServiceProviderResponseDTO createOwnerServiceProvider(ServiceProviderRequestDTO request) {
        return serviceProviderService.createServiceProvider(request);
    }

    // Update
    @Transactional
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto) {
        String username = authenticationFacade.getUsername();
        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Service.class,id));

        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, existing.getId());
        if(permissions == null || !permissions.contains(ProviderPermissionEnum.UPDATE))
            throw new AuthenticationException("Member doesn't have permission to update service");

        // Remove existing tag associations
        for (Tag tag : new ArrayList<>(existing.getTags())) {
            tag.getServices().remove(existing);
        }
        existing.getTags().clear();

        // Prepare new tags and associations
        List<Tag> newTags = tagService.findOrCreateTagsByNames(dto.tagNames());
        linkServiceToTags(existing, newTags);

        serviceMapper.updateEntityFromDto(dto, existing);
        serviceRepository.save(existing);
        return serviceMapper.toDto(existing, permissions);
    }

    // Read all
    public List<ServiceResponseDTO> getAll() {
        String username =  authenticationFacade.getUsername();
        return serviceRepository.findAll()
                .stream()
                .map(service ->  serviceMapper.toDto(service,
                        getPermissionsByProviderUsernameAndServiceId(username, service.getId())
                        ))
                .collect(Collectors.toList());
    }

    // Read one
    public ServiceResponseDTO getById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Service.class,id));
        String username =  authenticationFacade.getUsername();
        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, id);
        if(permissions == null || !permissions.contains(ProviderPermissionEnum.READ))
            throw new AuthenticationException("Member doesn't have permission to read service");
        return serviceMapper.toDto(service, getPermissionsByProviderUsernameAndServiceId(username, service.getId()));
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        String username =  authenticationFacade.getUsername();
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Service.class, id));
        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, id);
        if(permissions == null || !permissions.contains(ProviderPermissionEnum.DELETE))
            throw new AuthenticationException("Member doesn't have permission to delete service");

        service.removeAllTags();
        serviceRepository.delete(service);
    }

    private void linkServiceToTags(Service service, List<Tag> tags) {
        service.setTags(tags);
        for (Tag tag : tags) {
            tag.getServices().add(service);
        }
    }

    public List<ProviderPermissionEnum> getPermissionsByProviderUsernameAndServiceId(String username, Long serviceId){
        if(!hasServiceProvider(username, serviceId))
            return null;
        return serviceProviderService.getPermissionsByProviderUsernameAndServiceId(username, serviceId);
    }
    private boolean hasServiceProvider(String username, Long serviceId){
        return serviceProviderService.existsByServiceIdAndProviderUsername(serviceId, username);
    }

    public Page<ServiceResponseDTO> searchServices(String name, Double priceMin, Double priceMax, List<String> tagNames, Pageable pageable) {
        String username = authenticationFacade.getUsername();

        Specification<Service> spec = Specification.where(null); // start with no specifications, add each specification after if not null/empty

        spec = addIfPresent(spec, name != null && !name.isBlank(), () -> ServiceSpecifications.hasNameLike(name));
        spec = addIfPresent(spec, tagNames != null && !tagNames.isEmpty(), () -> ServiceSpecifications.hasAnyTagNameLike(tagNames));
        spec = addIfPresent(spec, priceMin != null, () -> ServiceSpecifications.hasPriceGreaterThanOrEqual(priceMin));
        spec = addIfPresent(spec, priceMax != null, () -> ServiceSpecifications.hasPriceLessThanOrEqual(priceMax));

        return serviceRepository.findAll(spec, pageable)
                .map(service ->  serviceMapper.toDto(service,
                        getPermissionsByProviderUsernameAndServiceId(username, service.getId())
                ));
    }

    private Specification<Service> addIfPresent(Specification<Service> spec, boolean condition, Supplier<Specification<Service>> supplier) {
        return condition ? spec.and(supplier.get()) : spec; // add specification on supplier, if the condition is met
    }
    /*
    @Transactional
    public ServiceResponseDTO updateMemberPermissions(Long serviceId, Long memberId, List<ProviderPermissionEnum> permissions){
        ServiceProvider serviceProvider = serviceProviderService.getByServiceIdAndMemberId(serviceId, memberId);
        serviceProviderService.deleteAllPermissions(serviceProvider);
        serviceProviderService.addPermissions(serviceProvider, permissions);
        return getById(serviceId);
    }
    */
    public Service getServiceEntityById(Long id) {
        return serviceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Service.class, id));
    }

    public boolean existsById(Long serviceId) {
        return serviceRepository.existsById(serviceId);
    }

}