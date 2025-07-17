package com.academy.dtos.availability;

import com.academy.models.Availability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "isException", source = "exception")
    AvailabilityResponseDTO toResponseDTOWithMember(Availability availability);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "exception", constant = "false")
    Availability toEntity(AvailabilityRequestDTO requestDTO);
}