package com.academy.dtos.appointment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.academy.models.Appointment;

@Mapper(componentModel = "spring")
public abstract class AppointmentMapper {

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "serviceProvider.id", target = "serviceProviderId")
    public abstract AppointmentResponseDTO toResponseDTO(Appointment appointment);

    @Mapping(source = "memberId", target = "member.id")
    @Mapping(source = "serviceProviderId", target = "serviceProvider.id")
    public abstract Appointment toEntity(AppointmentRequestDTO requestDTO);
}