package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.service.ServiceMapper;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.AuthenticationException;
//import com.academy.exceptions.ServiceNotFoundException;
import com.academy.models.Member;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Tag;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.ServiceTypeRepository;
import com.academy.repositories.TagRepository;
import com.academy.utils.Utils;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceProviderService serviceProviderService;
    private final ServiceRepository serviceRepository;
    private final TagRepository tagRepository;
    private final ServiceMapper serviceMapper;
    private final AuthenticationFacade authenticationFacade;
    private final MemberService memberService;
    private final TagService tagService;
    private final ServiceTypeRepository serviceTypeRepository;
    private final PlatformTransactionManager transactionManager;
    public ServiceService(ServiceRepository serviceRepository,
                          TagRepository tagRepository,
                          ServiceMapper serviceMapper,
                          ServiceProviderService serviceProviderService,
                          AuthenticationFacade authenticationFacade,
                          MemberService memberService,
                          TagService tagService,
                          ServiceTypeRepository serviceTypeRepository,
                          PlatformTransactionManager transactionManager) {
        this.serviceRepository = serviceRepository;
        this.tagRepository = tagRepository;
        this.serviceMapper = serviceMapper;
        this.serviceProviderService = serviceProviderService;
        this.authenticationFacade = authenticationFacade;
        this.memberService = memberService;
        this.tagService = tagService;
        this.serviceTypeRepository = serviceTypeRepository;
        this.transactionManager = transactionManager;
    }

    // Create
    @Transactional
    public ServiceResponseDTO create(ServiceRequestDTO dto) throws AuthenticationException, BadRequestException {
        Member member = memberService.getMemberByUsername(authenticationFacade.getUsername());
        Service service = serviceMapper.toEntity(dto, member.getId());

        List<Tag> tags = tagService.findOrCreateTagsByNames(dto.tagNames());
        linkServiceToTags(service, tags); // Set up the bidirectional link

        Service savedService = serviceRepository.save(service);

        createOwnerServiceProvider(member.getId(), service.getId());
        return serviceMapper.toDto(savedService, getPermissionsByProviderUsernameAndServiceId(member.getUsername(), savedService.getId()));
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
            return new ArrayList<ProviderPermissionEnum>();
        return serviceProviderService.getPermissionsByProviderUsernameAndServiceId(username, serviceId);
    }
    //TODO ver isto que nao esta a ser usado
    public List<ProviderPermissionEnum> getPermissionsByProviderIdAndServiceId(Long id, Long serviceId){
        if(!hasServiceProvider(id, serviceId))
            return new ArrayList<ProviderPermissionEnum>();
        return serviceProviderService.getPermissionsByProviderIdAndServiceId(id, serviceId);
    }
    private boolean hasServiceProvider(String username, Long serviceId){
        return serviceProviderService.existsByServiceIdAndProviderUsername(serviceId, username);
    }
    private boolean hasServiceProvider(Long id, Long  serviceId){
        return serviceProviderService.existsByServiceIdAndProviderId(id, serviceId);
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
        Member memberToBeUpdated =  memberService.getMemberById(memberToBeUpdatedId);
        List<ProviderPermissionEnum> oldPermissions = getPermissionsByProviderUsernameAndServiceId(memberToBeUpdated.getUsername(), serviceId);
        List<ProviderPermissionEnum> updaterPermissions = getPermissionsByProviderUsernameAndServiceId(updaterUsername, serviceId);

        validateUpdateOfPermissions(updaterPermissions, oldPermissions, newPermissions, updaterId, memberToBeUpdatedId);

        serviceProviderService.deleteAllPermissions(serviceProvider);
        serviceProviderService.addPermissions(serviceProvider, newPermissions);
        return getById(serviceId);
    }

    private ServiceProviderResponseDTO createOwnerServiceProvider(Long memberId, Long serviceId) throws AuthenticationException, BadRequestException {
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

}