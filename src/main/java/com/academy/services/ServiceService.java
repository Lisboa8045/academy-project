package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.service.ServiceMapper;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.models.member.Member;
import com.academy.models.service.Service;
import com.academy.models.service.ServiceImages;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.ServiceRepository;
import com.academy.specifications.ServiceSpecifications;
import com.academy.utils.Utils;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.hibernate.collection.spi.PersistentBag;
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

    @Transactional
    public Service createToEntity(ServiceRequestDTO dto) throws BadRequestException {
        Member member = memberService.getMemberByUsername(authenticationFacade.getUsername());
        Service service = serviceMapper.toEntity(dto, member.getId());

        linkServiceToType(service, dto.serviceTypeName());
        linkServiceToTags(service, dto.tagNames());

        Service savedService = serviceRepository.save(service);
        createAndLinkServiceOwner(savedService, member.getId());
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
        Service existing = getServiceEntityById(id);

        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, existing.getId());
        checkIfHasPermission(permissions,ProviderPermissionEnum.UPDATE, "update service");

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
        return serviceMapper.toDto(service, getPermissionsByProviderUsernameAndServiceId(username, service.getId()));
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        String username =  authenticationFacade.getUsername();
        Service service = getServiceEntityById(id);
        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, id);
        checkIfHasPermission(permissions,ProviderPermissionEnum.DELETE, "delete service");

        cleanUpService(service);
        serviceRepository.delete(service);
    }

    private void linkServiceToTags(Service service, List<String> tagNames) {
        removeAllTagLinks(service);

        List<Tag> tags = tagService.findOrCreateTagsByNames(tagNames);
        service.setTags(tags);
        for (Tag tag : tags) {
            List<Service> services = tag.getServices();

            if (services == null || services instanceof PersistentBag) {
                List<Service> modifiableServices = new ArrayList<>();
                if (services != null) {
                    modifiableServices.addAll(services);
                }
                modifiableServices.add(service);
                tag.setServices(modifiableServices);
            } else {
                services.add(service);
            }
        }
    }

    private void linkServiceToType(Service service, String serviceTypeName) {
        ServiceType previousType = service.getServiceType();
        if (previousType != null) {
            removeServiceTypeLink(service);
        }
        System.out.println("AA" + serviceTypeName);
        ServiceType type = serviceTypeService.getServiceTypeEntityByName(serviceTypeName);
        service.setServiceType(type);
        type.getServices().add(service);
    }

    private void createAndLinkServiceOwner(Service service, Long memberId) throws BadRequestException {
        ServiceProvider owner = createOwnerServiceProvider(memberId, service.getId());
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

    public Page<ServiceResponseDTO> searchServices(String name, Double minPrice, Double maxPrice,
                                                   Integer minDuration, Integer maxDuration, Boolean negotiable, String serviceTypeName, Pageable pageable) {
        String username = authenticationFacade.getUsername();

        Specification<Service> spec = Specification.where(null); // start with no specifications, add each specification after if not null/empty

        spec = addIfPresent(spec, name != null && !name.isBlank(), () -> ServiceSpecifications.nameOrTagMatches(name));
        spec = addIfPresent(spec, minPrice != null, () -> ServiceSpecifications.hasPriceGreaterThanOrEqual(minPrice));
        spec = addIfPresent(spec, maxPrice != null, () -> ServiceSpecifications.hasPriceLessThanOrEqual(maxPrice));
        spec = addIfPresent(spec, minDuration != null, () -> ServiceSpecifications.hasDurationGreaterThanOrEqual(minDuration));
        spec = addIfPresent(spec, maxDuration != null, () -> ServiceSpecifications.hasDurationLessThanOrEqual(maxDuration));
        spec = addIfPresent(spec, negotiable != null, () -> ServiceSpecifications.canNegotiate(negotiable));
        spec = addIfPresent(spec, serviceTypeName != null, () -> ServiceSpecifications.hasServiceType(serviceTypeName));

        return serviceRepository.findAll(spec, pageable)
                .map(service ->  serviceMapper.toDto(service,
                        getPermissionsByProviderUsernameAndServiceId(username, service.getId())
                ));
    }
    private Specification<Service> addIfPresent(Specification<Service> spec, boolean condition, Supplier<Specification<Service>> supplier) {
        return condition ? spec.and(supplier.get()) : spec; // add specification on supplier, if the condition is met
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
        checkIfHasPermission(updaterPermissions, ProviderPermissionEnum.UPDATE_PERMISSIONS, "update permissions");

        if(updaterId == memberToBeUpdatedId)
            throw new BadRequestException("Cannot edit your own permissions");
        if(isOwnerBeingUpdated)
            throw new BadRequestException("Cannot edit owner's permissions");

        ServiceProviderService.checkIfValidPermissions(newPermissions);
    }

    private void checkIfHasPermission(List<ProviderPermissionEnum> permissions, ProviderPermissionEnum permission, String permissionName) {
        if (!Utils.hasPermission(permissions, permission))
            throw new AuthenticationException("Member doesn't have permission to " + permissionName);
    }
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

    private void unlinkAndDisableServiceProviders(Service service) {
        List<ServiceProvider> providers = new ArrayList<>(service.getServiceProviders());

        for (ServiceProvider provider : providers) {
            provider.setService(null);
            provider.setActive(false);
        }
    }
    private void cleanUpService(Service service) {
        removeAllTagLinks(service);
        removeServiceTypeLink(service);
        unlinkAndDisableServiceProviders(service);
    }

    public Page<ServiceResponseDTO> getServicesByMemberId(Long memberId, Pageable pageable) {
       return serviceRepository.queryServicesByMemberId(memberId, pageable).map(service ->  serviceMapper.toDto(service,
               getPermissionsByProviderIdAndServiceId(memberId, service.getId())));
    }

    // este saveImages será para usado depois para o endpoint de criação do serviço
    public Service saveImages(Long id, List<ServiceImages> images) {
        serviceRepository.findById(id).map(s -> {
                    s.setImages(images);
                    serviceRepository.save(s);
                    return s;
                })
                .orElseThrow(() -> new EntityNotFoundException(Service.class, id));
        return null;
    }

}