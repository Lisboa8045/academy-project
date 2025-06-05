package com.academy.dtos.service;

import com.academy.models.Tag;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.repositories.MemberRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ServiceMapper {

    @Autowired
    protected MemberRepository memberRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "serviceType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(expression = "java(memberRepository.findById(ownerId).orElseThrow())", target ="owner")
    public abstract Service toEntity(ServiceRequestDTO dto, Long ownerId);

    @Mapping(source = "service.tags", target = "tagNames", qualifiedByName = "mapTagsToNames")
    @Mapping(source = "service.serviceType.name", target = "serviceTypeName")
    @Mapping(expression = "java(service.getOwner().getId())", target = "ownerId")
    @Mapping(expression = "java(permissions)", target = "permissions")
    public abstract ServiceResponseDTO toDto(Service service, List<ProviderPermissionEnum> permissions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "serviceType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntityFromDto(ServiceRequestDTO dto, @MappingTarget Service service);

    @Named("mapTagsToNames")
    protected List<String> mapTagsToNames(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    @Named("mapServicesToDTOs")
    public List<ServiceResponseDTO> mapServicesToDTOs(List<Service> services) {
        return services.stream()
                .map(service -> toDto(service, null))
                .collect(Collectors.toList());
    }
}
