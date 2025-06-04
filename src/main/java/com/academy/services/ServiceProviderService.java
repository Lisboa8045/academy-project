// ServiceProviderService.java
package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.service_provider.ServiceProviderMapper;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Member;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.ServiceProviderRepository;
import com.academy.repositories.ServiceRepository;
import com.academy.utils.Utils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderMapper serviceProviderMapper;
    private final MemberRepository memberRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderPermissionService providerPermissionService;
    private final MemberService memberService;
    private final AuthenticationFacade authenticationFacade;


    @Autowired
    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository,
                                  ServiceProviderMapper serviceProviderMapper,
                                  MemberRepository memberRepository,
                                  ServiceRepository serviceRepository,
                                  ProviderPermissionService providerPermissionService,
                                  MemberService memberService,
                                  AuthenticationFacade authenticationFacade) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceProviderMapper = serviceProviderMapper;
        this.memberRepository = memberRepository;
        this.serviceRepository = serviceRepository;
        this.providerPermissionService = providerPermissionService;
        this.memberService = memberService;
        this.authenticationFacade = authenticationFacade;
    }

    public static void checkIfValidPermissions(List<ProviderPermissionEnum> newPermissions) throws BadRequestException {
        if(newPermissions.contains(ProviderPermissionEnum.OWNER))
            throw new BadRequestException("Cannot give Owner Permission");
    }

    public List<ServiceProviderResponseDTO> getAllServiceProviders() {
        return serviceProviderRepository.findAll().stream()
                .map(serviceProviderMapper::toResponseDTO)
                .toList();
    }
    public ServiceProvider getById(Long id){
        Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findById(id);
        if(serviceProvider.isEmpty())
            throw new EntityNotFoundException(ServiceProvider.class, id);
        return serviceProvider.get();
    }

    public Optional<ServiceProviderResponseDTO> getServiceProviderById(long id) {
        return serviceProviderRepository.findById(id)
                .map(serviceProviderMapper::toResponseDTO);
    }

    @Transactional
    public ServiceProviderResponseDTO createServiceProvider(ServiceProviderRequestDTO dto) throws AuthenticationException, BadRequestException {
        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new EntityNotFoundException(Member.class, dto.memberId()));

        com.academy.models.service.Service service = serviceRepository.findById(dto.serviceId())
                .orElseThrow(() -> new EntityNotFoundException(com.academy.models.service.Service.class, dto.serviceId()));
        String username = authenticationFacade.getUsername();
        Member loggedMember = memberService.getMemberByUsername(username);

        if(!checkIfHasPermissionToAddServiceProvider(loggedMember, service, dto.isServiceCreation()))
            throw new AuthenticationException("You do not have permission to create a Service Provider");

        ServiceProvider serviceProvider = serviceProviderMapper.toEntity(dto);
        serviceProvider.setProvider(member);
        serviceProvider.setService(service);

        ServiceProvider saved = serviceProviderRepository.save(serviceProvider);

        validatePermissions(dto.permissions(), dto.isServiceCreation());

        ServiceProvider serviceProviderWithPermissions = providerPermissionService.createPermissionsViaList(dto.permissions(),saved);
        serviceProviderRepository.save(serviceProviderWithPermissions);
        return serviceProviderMapper.toResponseDTO(serviceProviderWithPermissions);
    }

    private boolean checkIfHasPermissionToAddServiceProvider(Member loggedMember, com.academy.models.service.Service service, boolean isServiceCreation){
        if(isServiceCreation)
            return true;
        ServiceProvider loggedMemberServiceProvider;
        try{
            loggedMemberServiceProvider = getByServiceIdAndMemberId(service.getId(), loggedMember.getId());
        }catch(EntityNotFoundException e){
            return false;
        }
        List<ProviderPermissionEnum> permissions = getPermissions(loggedMemberServiceProvider.getId());
        if(!Utils.hasPermission(permissions, ProviderPermissionEnum.ADD_SERVICE_PROVIDER))
            return false;

        return true;
    }
    private void validatePermissions(List<ProviderPermissionEnum> permissions, boolean isServiceCreation) throws BadRequestException {
        if(!isServiceCreation && permissions.contains(ProviderPermissionEnum.OWNER))
            throw new BadRequestException("Can't give the Owner permission to a new worker!");
    }


    public ServiceProviderResponseDTO updateServiceProvider(long id, ServiceProviderRequestDTO details) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));


//        if(details.getMemberId() != null){
//            Member member = memberRepository.findById(details.getMemberId())
//                    .orElseThrow(()-> new EntityNotFoundException(Member.class, details.getMemberId()));
//            serviceProvider.setProvider(member);
//        }

        if(details.serviceId() != null) {
            com.academy.models.service.Service service = serviceRepository.findById(details.serviceId())
                    .orElseThrow(()-> new EntityNotFoundException(ServiceProvider.class, details.serviceId()));
            serviceProvider.setService(service);
        }

        serviceProvider = serviceProviderRepository.save(serviceProvider);
        if (details.permissions() != null) {
            providerPermissionService.createPermissionsViaList(details.permissions(), serviceProvider);
        }
        return serviceProviderMapper.toResponseDTO(serviceProvider);
    }

    public void deleteServiceProvider(long id) {
        if(!serviceProviderRepository.existsById(id)) throw new EntityNotFoundException(ServiceProvider.class, id);
        serviceProviderRepository.deleteById(id);
    }

    public List<ProviderPermissionEnum> getPermissionsByProviderUsernameAndServiceId(String username, Long serviceId){
        ServiceProvider serviceProvider= getServiceProviderByProviderUsernameAndServiceID(username, serviceId);
        return getPermissions(serviceProvider.getId());
    }
    public List<ProviderPermissionEnum> getPermissionsByProviderIdAndServiceId(Long id, Long serviceId){
        ServiceProvider serviceProvider= getServiceProviderByProviderIdAndServiceID(id, serviceId);
        return getPermissions(serviceProvider.getId());
    }

    private ServiceProvider getServiceProviderByProviderUsernameAndServiceID(String username, Long serviceId) {
        Optional<ServiceProvider> optionalServiceProvider =
                serviceProviderRepository.findByProviderUsernameAndServiceId(username, serviceId);
        if(optionalServiceProvider.isEmpty())
            throw new EntityNotFoundException(ServiceProvider.class,
                    " not found for user " + username + " and serviceId " + serviceId);
        return optionalServiceProvider.get();
    }
    private ServiceProvider getServiceProviderByProviderIdAndServiceID(Long id, Long serviceId) {
        Optional<ServiceProvider> optionalServiceProvider =
                serviceProviderRepository.findByProviderIdAndServiceId(id, serviceId);
        if(optionalServiceProvider.isEmpty())
            throw new EntityNotFoundException(ServiceProvider.class,
                    " not found for user with id" + id + " and serviceId " + serviceId);
        return optionalServiceProvider.get();
    }

    public List<ProviderPermissionEnum> getPermissions(Long id){
        return providerPermissionService.getPermissions(id);
    }

    public boolean existsByServiceId(Long serviceId) {
        return serviceProviderRepository.existsByServiceId(serviceId);
    }

    public boolean existsByServiceIdAndProviderUsername(Long serviceId, String username) {
        return serviceProviderRepository.existsByServiceIdAndProviderUsername(serviceId, username);
    }
    public boolean existsByServiceIdAndProviderId(Long serviceId, Long id) {
        return serviceProviderRepository.existsByServiceIdAndProviderId(serviceId, id);
    }

    public ServiceProvider getByServiceIdAndMemberId(Long serviceId, Long memberId){
        Optional<ServiceProvider> optionalServiceProvider =  serviceProviderRepository.findByServiceIdAndProviderId(serviceId, memberId);
        if(optionalServiceProvider.isEmpty())
            throw new EntityNotFoundException(ServiceProvider.class, serviceId);
        return optionalServiceProvider.get();
    }


    public void deleteAllPermissions(ServiceProvider serviceProvider) {
        providerPermissionService.deleteAllByServiceProvider(serviceProvider);
    }

    public void addPermissions(ServiceProvider serviceProvider,List<ProviderPermissionEnum> permissions){
            providerPermissionService.createPermissionsViaList(permissions, serviceProvider);
    }

}
