package com.academy.dtos.availability;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.academy.models.Availability;

@Mapper(componentModel = "spring")
public abstract class AvailabilityMapper {

    @Mapping(target = "memberId", source = "member.id")
    public abstract AvailabilityResponseDTO toResponseDTOWithMember(Availability availability);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Availability toEntity(AvailabilityRequestDTO requestDTO);
}
