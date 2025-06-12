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
import com.academy.utils.Utils;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.context.annotation.Lazy;
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

    @Lazy
    private final ServiceProviderService serviceProviderService;
    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final AuthenticationFacade authenticationFacade;
    private final MemberService memberService;
    private final TagService tagService;
    private final ServiceTypeService serviceTypeService;

    public ServiceService(ServiceRepository serviceRepository,
                          ServiceMapper serviceMapper,
                          @Lazy ServiceProviderService serviceProviderService,
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

    public Service createToEntity(ServiceRequestDTO dto) throws BadRequestException {
        Member member = memberService.getMemberByUsername(authenticationFacade.getUsername());
        Service service = serviceMapper.toEntity(dto, member.getId());

        ServiceType type = serviceTypeService.findByNameOrThrow(dto.serviceTypeName());
        linkServiceToType(service, type);

        List<Tag> tags = tagService.findOrCreateTagsByNames(dto.tagNames());
        linkServiceToTags(service, tags); // Set up the bidirectional link

        Service savedService = serviceRepository.save(service);

        ServiceProvider owner = createOwnerServiceProvider(member.getId(), service.getId());
        linkServiceToOwnerAsProvider(service, owner);
        return savedService;
    }

    // Create
    @Transactional
    public ServiceResponseDTO create(ServiceRequestDTO dto) throws AuthenticationException, BadRequestException {
        Service service = createToEntity(dto);
        String username = authenticationFacade.getUsername();
        return serviceMapper.toDto(service, getPermissionsByProviderUsernameAndServiceId(username, service.getId()));
    }

    // Update
    @Transactional
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto) throws AuthenticationException{
        String username = authenticationFacade.getUsername();
        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Service.class,id));

        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, existing.getId());
        if(!checkIfHasPermission(permissions,ProviderPermissionEnum.UPDATE))
            throw new AuthenticationException("Member doesn't have permission to update service");

        ServiceType type = serviceTypeService.findByNameOrThrow(dto.serviceTypeName());
        linkServiceToType(existing, type);

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
        Service service = getEntityById(id);
        String username =  authenticationFacade.getUsername();
        return serviceMapper.toDto(service, getPermissionsByProviderUsernameAndServiceId(username, service.getId()));
    }
    public Service getEntityById(Long id){
        return serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Service.class,id));
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        String username =  authenticationFacade.getUsername();
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Service.class, id));
        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, id);
        if(!checkIfHasPermission(permissions,ProviderPermissionEnum.DELETE))
            throw new AuthenticationException("Member doesn't have permission to delete service");

        service.removeAllLinks();
        deleteServiceProviders(service);
        serviceRepository.delete(service);
    }

    private void linkServiceToTags(Service service, List<Tag> tags) {
        service.setTags(tags);
        for (Tag tag : tags) {
            tag.getServices().add(service);
        }
    }

    private void linkServiceToType(Service service, ServiceType type) {
        service.setServiceType(type);
        type.getServices().add(service);
    }

    private void linkServiceToOwnerAsProvider(Service service, ServiceProvider owner) {
        service.getServiceProviders().add(owner);
        owner.setService(service);
    }

    public List<ProviderPermissionEnum> getPermissionsByProviderUsernameAndServiceId(String username, Long serviceId){
        if(!hasServiceProvider(username, serviceId))
            return Collections.emptyList();
        return serviceProviderService.getPermissionsByProviderUsernameAndServiceId(username, serviceId);
    }
    public List<ProviderPermissionEnum> getPermissionsByProviderIdAndServiceId(Long providerId, Long serviceId){
        return hasServiceProvider(providerId, serviceId) ?
                serviceProviderService.getPermissionsByProviderIdAndServiceId(providerId, serviceId)
        :
                Collections.emptyList();
    }
    private boolean hasServiceProvider(String username, Long serviceId){
        return serviceProviderService.existsByServiceIdAndProviderUsername(serviceId, username);
    }
    private boolean hasServiceProvider(Long id, Long  serviceId){
        return serviceProviderService.existsByServiceIdAndProviderId(id, serviceId);
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
    @Transactional
    public void deleteServiceProviders(Service service) {
        List<ServiceProvider> providers = new ArrayList<>(service.getServiceProviders());

        for (ServiceProvider provider : providers) {
            serviceProviderService.deleteServiceProvider(provider.getId());
        }
    }
    @Transactional
    public ServiceResponseDTO updateMemberPermissions(Long serviceId, Long memberToBeUpdatedId, List<ProviderPermissionEnum> newPermissions) throws AuthenticationException, BadRequestException {
        String updaterUsername =   authenticationFacade.getUsername();
        Long updaterId = memberService.getMemberByUsername(updaterUsername).getId();
        ServiceProvider serviceProvider;
        try{
            serviceProvider = serviceProviderService.getByServiceIdAndMemberId(serviceId, memberToBeUpdatedId);
        }catch(EntityNotFoundException e){
            throw new BadRequestException("The service with id " +  serviceId + " does not have a Service Provider with the id " +memberToBeUpdatedId);
        }
        Member memberToBeUpdated =  memberService.getMemberEntityById(memberToBeUpdatedId);
        List<ProviderPermissionEnum> oldPermissions = getPermissionsByProviderUsernameAndServiceId(memberToBeUpdated.getUsername(), serviceId);
        List<ProviderPermissionEnum> updaterPermissions = getPermissionsByProviderUsernameAndServiceId(updaterUsername, serviceId);

        validateUpdateOfPermissions(updaterPermissions, oldPermissions, newPermissions, updaterId, memberToBeUpdatedId);

        serviceProviderService.deleteAllPermissions(serviceProvider.getId());
        serviceProviderService.addPermissions(serviceProvider, newPermissions);
        return getById(serviceId);
    }
    private ServiceProvider createOwnerServiceProvider(Long memberId, Long serviceId) throws AuthenticationException, BadRequestException {
        return serviceProviderService.createServiceProvider(new ServiceProviderRequestDTO(
                memberId,
                serviceId,
                Arrays.asList(ProviderPermissionEnum.values()),
                true
        ));
    }
    private void validateUpdateOfPermissions(List<ProviderPermissionEnum> updaterPermissions,
                                             List<ProviderPermissionEnum> oldPermissions,
                                             List<ProviderPermissionEnum> newPermissions,
                                             Long updaterId,
                                             Long memberToBeUpdatedId) throws BadRequestException {
        boolean isOwnerBeingUpdated = oldPermissions.contains(ProviderPermissionEnum.OWNER);
        if(!checkIfHasPermission(updaterPermissions, ProviderPermissionEnum.UPDATE_PERMISSIONS))
            throw new AuthenticationException("Member does not have permission to update permissions");

        if(updaterId == memberToBeUpdatedId)
            throw new BadRequestException("Cannot edit your own permissions");
        if(isOwnerBeingUpdated)
            throw new BadRequestException("Cannot edit owner's permissions");

        ServiceProviderService.checkIfValidPermissions(newPermissions);
    }

    private boolean checkIfHasPermission(List<ProviderPermissionEnum> permissions, ProviderPermissionEnum permission ) {
        return Utils.hasPermission(permissions, permission);
    }
    public Service getServiceEntityById(Long id) {
        return serviceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Service.class, id));
    }

    public boolean existsById(Long serviceId) {
        return serviceRepository.existsById(serviceId);
    }

}