package com.academy.dtos.appointment;


import com.academy.models.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public abstract class AppointmentMapper {
    @Mappings ({
            @Mapping(source= "serviceProvider.id", target = "serviceProviderId"),
            @Mapping(source= "member.id", target = "memberId")
    })
    public abstract AppointmentResponseDTO toResponseDTO(Appointment appointment);


    public abstract Appointment toEntity(AppointmentRequestDTO appointmentRequestDTO);
}
