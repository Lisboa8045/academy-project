package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.service.ServiceMapper;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Member;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.ServiceRepository;
import com.academy.specifications.ServiceSpecifications;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceProviderService serviceProviderService;
    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final AuthenticationFacade authenticationFacade;
    private final MemberService memberService;
    private final TagService tagService;
    private final ServiceTypeService serviceTypeService;

    public ServiceService(ServiceRepository serviceRepository,
                          ServiceMapper serviceMapper,
                          ServiceProviderService serviceProviderService,
                          AuthenticationFacade authenticationFacade,
                          MemberService memberService,
                          TagService tagService,
                          ServiceTypeService serviceTypeService) {
        this.serviceRepository = serviceRepository;
        this.serviceMapper = serviceMapper;
        this.serviceProviderService = serviceProviderService;
        this.authenticationFacade = authenticationFacade;
        this.memberService = memberService;
        this.tagService = tagService;
        this.serviceTypeService = serviceTypeService;
    }

    // Create
    @Transactional
    public ServiceResponseDTO create(ServiceRequestDTO dto) {
        Member member = memberService.getMemberByUsername(authenticationFacade.getUsername());
        Service service = serviceMapper.toEntity(dto, member.getId());

        linkServiceToType(service, dto.serviceTypeName());
        linkServiceToTags(service, dto.tagNames());

        Service savedService = serviceRepository.save(service);
        createAndLinkServiceOwner(savedService, member.getId());

        return serviceMapper.toDto(savedService, getPermissionsByProviderUsernameAndServiceId(member.getUsername(), savedService.getId()));
    }

    private ServiceProvider createOwnerServiceProvider(ServiceProviderRequestDTO request) {
        return serviceProviderService.createServiceProvider(request);
    }

    // Update
    @Transactional
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto) {
        String username = authenticationFacade.getUsername();
        Service existing = getServiceEntityById(id);

        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, existing.getId());
        if(permissions == null || !permissions.contains(ProviderPermissionEnum.UPDATE))
            throw new AuthenticationException("Member doesn't have permission to update service");

        linkServiceToType(existing, dto.serviceTypeName());
        linkServiceToTags(existing, dto.tagNames());

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
        Service service = getServiceEntityById(id);
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
        Service service = getServiceEntityById(id);
        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, id);
        if(permissions == null || !permissions.contains(ProviderPermissionEnum.DELETE))
            throw new AuthenticationException("Member doesn't have permission to delete service");

        cleanUpService(service);
        serviceRepository.delete(service);
    }

    private void linkServiceToTags(Service service, List<String> tagNames) {
        removeAllTagLinks(service);

        List<Tag> tags = tagService.findOrCreateTagsByNames(tagNames);
        service.setTags(tags);
        for (Tag tag : tags) {
            tag.getServices().add(service);
        }
    }

    private void linkServiceToType(Service service, String serviceTypeName) {
        ServiceType previousType = service.getServiceType();
        if (previousType != null) {
            removeServiceTypeLink(service);
        }

        ServiceType type = serviceTypeService.getServiceTypeEntityByName(serviceTypeName);
        service.setServiceType(type);
        type.getServices().add(service);
    }

    private void createAndLinkServiceOwner(Service service, Long memberId) {
        ServiceProvider owner = createOwnerServiceProvider(new ServiceProviderRequestDTO(
                memberId,
                service.getId(),
                Arrays.asList(ProviderPermissionEnum.values())
        ));
        service.getServiceProviders().add(owner);
        owner.setService(service);
    }

    public List<ProviderPermissionEnum> getPermissionsByProviderUsernameAndServiceId(String username, Long serviceId){
        if(!hasServiceProvider(username, serviceId))
            return Collections.emptyList();
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

    public List<Service> getServiceEntitiesByIds(List<Long> ids) {
        return serviceRepository.findAllById(ids);
    }

    public boolean existsById(Long serviceId) {
        return serviceRepository.existsById(serviceId);
    }

    private void removeAllTagLinks(Service service) {
        for (Tag tag : new ArrayList<>(service.getTags())) {
            tag.getServices().remove(service);
        }
        service.getTags().clear();
    }

    private void removeServiceTypeLink(Service service) {
        service.getServiceType().getServices().remove(service);
    }

    private void deleteServiceProviders(Service service) {
        List<ServiceProvider> providers = new ArrayList<>(service.getServiceProviders());

        for (ServiceProvider provider : providers) {
            serviceProviderService.deleteServiceProvider(provider.getId());
        }
    }
    private void cleanUpService(Service service) {
        removeAllTagLinks(service);
        removeServiceTypeLink(service);
        deleteServiceProviders(service);
    }
}