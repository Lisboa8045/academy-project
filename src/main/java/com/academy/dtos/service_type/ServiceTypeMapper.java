package com.academy.dtos.service_type;

import com.academy.dtos.service.ServiceMapper;
import com.academy.models.ServiceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = ServiceMapper.class)
public abstract class ServiceTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ServiceType toEntity(ServiceTypeRequestDTO dto);

    @Mapping(source = "services", target = "services", qualifiedByName = "mapServicesToDTOs")
    public abstract ServiceTypeResponseDTO toDto(ServiceType entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntityFromDto(ServiceTypeRequestDTO dto, @MappingTarget ServiceType entity);
}