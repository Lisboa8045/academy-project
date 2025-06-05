// ServiceProviderService.java
package com.academy.services;

import com.academy.dtos.service_provider.ServiceProviderMapper;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Member;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.ServiceProviderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderMapper serviceProviderMapper;
    private final MemberService memberService;
    private final ServiceService serviceService;
    private final ProviderPermissionService providerPermissionService;

    @Autowired
    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository,
                                  ServiceProviderMapper serviceProviderMapper,
                                  MemberService memberService,
                                  ServiceService serviceService,
                                  ProviderPermissionService providerPermissionService) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceProviderMapper = serviceProviderMapper;
        this.memberService = memberService;
        this.serviceService = serviceService;
        this.providerPermissionService = providerPermissionService;
    }

    public List<ServiceProviderResponseDTO> getAllServiceProviders() {
        return serviceProviderRepository.findAll().stream()
                .map(serviceProviderMapper::toResponseDTO)
                .toList();

    }

    public Optional<ServiceProviderResponseDTO> getServiceProviderById(long id) {
        return serviceProviderRepository.findById(id)
                .map(serviceProviderMapper::toResponseDTO);
    }

    public ServiceProviderResponseDTO createServiceProviderWithDTO(ServiceProviderRequestDTO dto) {
        ServiceProvider serviceProvider = createServiceProvider(dto);
        return serviceProviderMapper.toResponseDTO(serviceProvider);
    }

    public ServiceProvider createServiceProvider(ServiceProviderRequestDTO dto) {
        Member member = memberService.getMemberEntityById(dto.memberId());

        Service service = serviceService.getServiceEntityById(dto.serviceId());

        //ProviderPermissionEnum permission = ProviderPermissionEnum.values()[dto.permissions()];

        ServiceProvider serviceProvider = serviceProviderMapper.toEntity(dto);
        serviceProvider.setProvider(member);
        serviceProvider.setService(service);

        ServiceProvider saved = serviceProviderRepository.save(serviceProvider);
        providerPermissionService.createPermissionsViaList(dto.permissions(),saved);

        return serviceProviderRepository.save(saved);
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
        ServiceProvider serviceProvider= getServiceProviderByProviderIdAndServiceID(username, serviceId);
        return getPermissions(serviceProvider.getId());
    }

    private ServiceProvider getServiceProviderByProviderIdAndServiceID(String username, Long serviceId) {
        Optional<ServiceProvider> optionalServiceProvider =
                serviceProviderRepository.findByProviderUsernameAndServiceId(username, serviceId);
        if(optionalServiceProvider.isEmpty())
            throw new EntityNotFoundException(ServiceProvider.class,
                    " not found for user " + username + " and serviceId " + serviceId);
        return optionalServiceProvider.get();
    }

    private List<ProviderPermissionEnum> getPermissions(Long id){
        return providerPermissionService.getPermissions(id);
    }

    public boolean existsByServiceId(Long serviceId) {
        return serviceProviderRepository.existsByServiceId(serviceId);
    }

    public boolean existsByServiceIdAndProviderUsername(Long serviceId, String username) {
        return serviceProviderRepository.existsByServiceIdAndProviderUsername(serviceId, username);
    }

    public List<Long> findMemberIdsByServiceId(Long serviceId) {
        return serviceProviderRepository.findMemberIdsByServiceId(serviceId);
    }

    public ServiceProvider getServiceProviderEntityById(long id){
        return serviceProviderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));
    }

}
