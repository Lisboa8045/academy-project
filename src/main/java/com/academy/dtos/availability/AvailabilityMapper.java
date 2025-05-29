package com.academy.dtos.availability;

import org.mapstruct.*;
import com.academy.models.Availability;

@Mapper(componentModel = "spring")
public abstract class AvailabilityMapper {

    @Mapping(target = "memberId", source = "member.id")
    public abstract AvailabilityResponseDTO toResponseDTOWithMember(Availability availability);

    public abstract Availability toEntity(AvailabilityRequestDTO requestDTO);

    @Mapping(target = "member.id", source = "memberId")
    public abstract Availability toEntityWithMember(AvailabilityRequestDTO requestDTO);
}