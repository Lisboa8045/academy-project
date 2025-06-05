// ServiceProviderService.java
package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.service_provider.ServiceProviderMapper;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Member;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.MemberRepository;
import com.academy.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import com.academy.repositories.ServiceProviderRepository;
import jakarta.transaction.Transactional;
import com.academy.repositories.ServiceRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@org.springframework.stereotype.Service
public class ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderMapper serviceProviderMapper;
    private final MemberService memberService;
    private final ServiceService serviceService;
    private final AuthenticationFacade authenticationFacade;
    private final ProviderPermissionService providerPermissionService;



    @Autowired
    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository,
                                  ServiceProviderMapper serviceProviderMapper,
                                  MemberRepository memberRepository,
                                  ServiceRepository serviceRepository,
                                  ProviderPermissionService providerPermissionService) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceProviderMapper = serviceProviderMapper;
        this.memberService = memberService;
        this.serviceService = serviceService;
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

    public ServiceProviderResponseDTO createServiceProviderWithDTO(ServiceProviderRequestDTO dto) throws BadRequestException {
        ServiceProvider serviceProvider = createServiceProvider(dto);
        return serviceProviderMapper.toResponseDTO(serviceProvider);
    }
    @Transactional
    public ServiceProviderResponseDTO createServiceProvider(ServiceProviderRequestDTO dto) throws BadRequestException {
        Member member = memberService.getMemberEntityById(dto.memberId());

        Service service = serviceService.getServiceEntityById(dto.serviceId());

        if(!checkIfHasPermissionToAddServiceProvider(loggedMember, service, dto.isServiceCreation()))
            throw new AuthenticationException("You do not have permission to create a Service Provider");

        ServiceProvider serviceProvider = serviceProviderMapper.toEntity(dto);
        serviceProvider.setProvider(member);
        serviceProvider.setService(service);

        ServiceProvider saved = serviceProviderRepository.save(serviceProvider);

        validatePermissions(dto.permissions(), dto.isServiceCreation());

        /*
                ServiceProvider saved = serviceProviderRepository.save(serviceProvider);
        providerPermissionService.createPermissionsViaList(dto.permissions(),saved);

        return serviceProviderRepository.save(saved);
         */
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
            Service service = serviceService.getServiceEntityById(details.serviceId());
            serviceProvider.setService(service);
        }

        serviceProvider = serviceProviderRepository.save(serviceProvider);
        if (details.permissions() != null) {
            providerPermissionService.createPermissionsViaList(details.permissions(), serviceProvider);
        }
        return serviceProviderMapper.toResponseDTO(serviceProvider);
    }

    @Transactional
    public void deleteServiceProvider(long id) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));

        providerPermissionService.deletePermissionsFromServiceProvider(serviceProvider);

        serviceProviderRepository.delete(serviceProvider);
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
    public List<Long> findMemberIdsByServiceId(Long serviceId) {
        return serviceProviderRepository.findMemberIdsByServiceId(serviceId);
    }

    public ServiceProvider getServiceProviderEntityById(long id){
        return serviceProviderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));
    }
}
