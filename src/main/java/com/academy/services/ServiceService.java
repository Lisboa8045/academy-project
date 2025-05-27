package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.service.ServiceMapper;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.ServiceNotFoundException;
import com.academy.models.Member;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.TagRepository;
import jakarta.transaction.Transactional;

import java.util.Arrays;
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
    public ServiceService(ServiceRepository serviceRepository,
                          TagRepository tagRepository,
                          ServiceMapper serviceMapper,
                          ServiceProviderService serviceProviderService,
                          AuthenticationFacade authenticationFacade,
                          MemberService memberService) {
        this.serviceRepository = serviceRepository;
        this.tagRepository = tagRepository;
        this.serviceMapper = serviceMapper;
        this.serviceProviderService = serviceProviderService;
        this.authenticationFacade = authenticationFacade;
        this.memberService = memberService;
    }

    // Create
    public ServiceResponseDTO create(ServiceRequestDTO dto) {
        Member member = memberService.getMemberByUsername(authenticationFacade.getUsername());
        Service service = serviceMapper.toEntity(dto, member.getId());
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
    public ServiceResponseDTO update(Long id, ServiceRequestDTO dto) {
        String username = authenticationFacade.getUsername();
        Member member = memberService.getMemberByUsername(username);
        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));
        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, existing.getId());
        if(permissions == null || !permissions.contains(ProviderPermissionEnum.UPDATE))
            throw new AuthenticationException("Member doesn't have permission to update service");

        Service updated = serviceMapper.toEntity(dto, member.getId());
        updated.setId(existing.getId());  // Retain existing ID
        updated = serviceRepository.save(updated);

        return serviceMapper.toDto(updated, getPermissionsByProviderUsernameAndServiceId(username, updated.getId()));
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
                .orElseThrow(() -> new ServiceNotFoundException(id));
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
                .orElseThrow(() -> new ServiceNotFoundException(id));
        List<ProviderPermissionEnum> permissions = getPermissionsByProviderUsernameAndServiceId(username, id);
        if(permissions == null || !permissions.contains(ProviderPermissionEnum.DELETE))
            throw new AuthenticationException("Member doesn't have permission to delete service");

        service.removeAllTags(); // Disassociate the tags from the service
        serviceRepository.delete(service);
    }

    public List<ProviderPermissionEnum> getPermissionsByProviderUsernameAndServiceId(String username, Long serviceId){
        if(!hasServiceProvider(username, serviceId))
            return null;
        return serviceProviderService.getPermissionsByProviderUsernameAndServiceId(username, serviceId);
    }
    private boolean hasServiceProvider(String username, Long serviceId){
        return serviceProviderService.existsByServiceIdAndProviderUsername(serviceId, username);
    }

}