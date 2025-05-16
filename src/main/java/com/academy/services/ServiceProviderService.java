// ServiceProviderService.java
package com.academy.services;

import com.academy.dtos.service_provider.ServiceProviderMapper;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_provider.ServiceProviderResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Member;
import com.academy.models.service_provider.ProviderPermission;
import com.academy.models.service_provider.ServiceProvider;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.ServiceProviderRepository;
import com.academy.repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderMapper serviceProviderMapper;
    private final MemberRepository memberRepository;
    private final ServiceRepository serviceRepository;



    @Autowired
    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository, ServiceProviderMapper serviceProviderMapper, MemberRepository memberRepository, ServiceRepository serviceRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceProviderMapper = serviceProviderMapper;
        this.memberRepository = memberRepository;
        this.serviceRepository = serviceRepository;
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

    public ServiceProviderResponseDTO createServiceProvider(ServiceProviderRequestDTO dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException(Member.class, dto.getMemberId()));

        com.academy.models.Service service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException(com.academy.models.Service.class, dto.getServiceId()));

        ProviderPermission permission = ProviderPermission.values()[dto.getPermission()];

        ServiceProvider serviceProvider = serviceProviderMapper.toEntity(dto);
        serviceProvider.setProvider(member);
        serviceProvider.setService(service);
        serviceProvider.setPermission(permission);

        ServiceProvider saved = serviceProviderRepository.save(serviceProvider);
        return serviceProviderMapper.toResponseDTO(saved);
    }


    public ServiceProviderResponseDTO updateServiceProvider(long id, ServiceProviderRequestDTO details) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));


//        if(details.getMemberId() != null){
//            Member member = memberRepository.findById(details.getMemberId())
//                    .orElseThrow(()-> new EntityNotFoundException(Member.class, details.getMemberId()));
//            serviceProvider.setProvider(member);
//        }

        if(details.getServiceId() != null) {
            com.academy.models.Service service = serviceRepository.findById(details.getServiceId())
                    .orElseThrow(()-> new EntityNotFoundException(ServiceProvider.class, details.getServiceId()));
            serviceProvider.setService(service);
        }

        if (details.getPermission() != null) {
            int ordinal = details.getPermission();

            ProviderPermission[] permissions = ProviderPermission.values();

            serviceProvider.setPermission(permissions[ordinal]);
        }
        return serviceProviderMapper.toResponseDTO(serviceProviderRepository.save(serviceProvider));
    }

    public void deleteServiceProvider(long id) {
        if(!serviceProviderRepository.existsById(id)) throw new EntityNotFoundException(ServiceProvider.class, id);
        serviceProviderRepository.deleteById(id);
    }
}
