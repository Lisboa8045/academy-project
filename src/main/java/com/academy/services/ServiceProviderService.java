package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.service_provider.ServiceProviderMapper;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.MemberNotFoundException;
import com.academy.models.member.Member;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.ServiceProviderRepository;
import com.academy.utils.Utils;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Optional;

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
                                  ProviderPermissionService providerPermissionService,
                                  MemberService memberService,
                                  @Lazy ServiceService serviceService,
                                  AuthenticationFacade authenticationFacade) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceProviderMapper = serviceProviderMapper;
        this.memberService = memberService;
        this.serviceService = serviceService;
        this.providerPermissionService = providerPermissionService;
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

    //TODO refactor deste método para dar return de um não Optional
    public Optional<ServiceProviderResponseDTO> getServiceProviderById(long id) {
        return serviceProviderRepository.findById(id)
                .map(serviceProviderMapper::toResponseDTO);
    }

    public ServiceProvider getServiceProviderByUsername(String username){
        Optional<ServiceProvider> optionalServiceProvider = serviceProviderRepository.findByProviderUsername(username);
        if(optionalServiceProvider.isEmpty())
            throw new MemberNotFoundException(username);
        return optionalServiceProvider.get();
    }

    public List<ServiceProvider> getAllByProviderId(Long id) {
        return serviceProviderRepository.findAllByProviderId(id);
    }

    @Transactional
    public ServiceProviderResponseDTO createServiceProviderWithDTO(ServiceProviderRequestDTO dto) throws BadRequestException {
        ServiceProvider serviceProvider = createServiceProvider(dto);
        return serviceProviderMapper.toResponseDTO(serviceProvider);
    }

    @Transactional
    public ServiceProvider createServiceProvider(ServiceProviderRequestDTO dto) throws BadRequestException {
        Member member = memberService.getMemberEntityById(dto.memberId());

        Service service = serviceService.getServiceEntityById(dto.serviceId());

        String loggedUsername = authenticationFacade.getUsername();
        Member loggedMember = memberService.getMemberByUsername(loggedUsername);

        if(!checkIfHasPermissionToAddServiceProvider(loggedMember, service, dto.isServiceCreation()))
            throw new AuthenticationException("You do not have permission to create a Service Provider");

        ServiceProvider serviceProvider = serviceProviderMapper.toEntity(dto);
        serviceProvider.setProvider(member);
        serviceProvider.setService(service);

        ServiceProvider saved = serviceProviderRepository.save(serviceProvider);

        validatePermissions(dto.permissions(), dto.isServiceCreation());

        ServiceProvider serviceProviderWithPermissions = providerPermissionService.createPermissionsViaList(dto.permissions(),saved);
        return serviceProviderRepository.save(serviceProviderWithPermissions);
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
        return Utils.hasPermission(permissions, ProviderPermissionEnum.ADD_SERVICE_PROVIDER);
    }

    private void validatePermissions(List<ProviderPermissionEnum> permissions, boolean isServiceCreation) throws BadRequestException {
        if(!isServiceCreation && permissions.contains(ProviderPermissionEnum.OWNER))
            throw new BadRequestException("Can't give the Owner permission to a new worker!");
    }

    @Transactional
    public ServiceProviderResponseDTO updateServiceProvider(long id, ServiceProviderRequestDTO details) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));

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
        serviceProvider.setActive(false);
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
    public ServiceProvider getServiceProviderByProviderIdAndServiceID(Long id, Long serviceId) {
        Optional<ServiceProvider> optionalServiceProvider =
                serviceProviderRepository.findByProviderIdAndServiceId(id, serviceId);
        if(optionalServiceProvider.isEmpty())
            throw new EntityNotFoundException(ServiceProvider.class,
                    " not found for user with id" + id + " and serviceId " + serviceId);
        return optionalServiceProvider.get();
    }

    public ServiceProviderResponseDTO getServiceProviderDTOByProviderIdAndServiceID(Long providerId, Long serviceId){
        ServiceProvider sp = getServiceProviderByProviderIdAndServiceID(providerId, serviceId);
        return serviceProviderMapper.toResponseDTO(sp);
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

    @Transactional
    public void deleteAllPermissions(Long serviceProviderId) {
        ServiceProvider serviceProvider = getServiceProviderEntityById(serviceProviderId);
        providerPermissionService.deleteAllByServiceProvider(serviceProvider.getId());
    }

    @Transactional
    public void addPermissions(ServiceProvider serviceProvider,List<ProviderPermissionEnum> permissions){
        providerPermissionService.createPermissionsViaList(permissions, serviceProvider);
    }

    public List<Long> findMemberIdsByServiceId(Long serviceId) {
        return serviceProviderRepository.findMemberIdsByServiceId(serviceId);
    }

    public ServiceProvider getServiceProviderEntityById(long id){
        return serviceProviderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));
    }

    public List<ServiceProvider> findProvidersByServiceIdAndPermission(Long serviceId, ProviderPermissionEnum permission) {
        return serviceProviderRepository.findProvidersByServiceIdAndPermission(serviceId, permission);
    }

}
