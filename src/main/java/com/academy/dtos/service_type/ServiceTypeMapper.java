package com.academy.dtos.service_type;

import com.academy.models.ServiceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class ServiceTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ServiceType toEntity(ServiceTypeRequestDTO dto);

    public abstract ServiceTypeResponseDTO toDto(ServiceType entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntityFromDto(ServiceTypeRequestDTO dto, @MappingTarget ServiceType entity);
}