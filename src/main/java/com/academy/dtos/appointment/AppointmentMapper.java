package com.academy.dtos.appointment;


import com.academy.models.appointment.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class AppointmentMapper {


    @Mapping(source= "serviceProvider.id", target = "serviceProviderId")
    public abstract AppointmentResponseDTO toResponseDTO(Appointment appointment);

    public abstract Appointment toEntity(AppointmentRequestDTO appointmentRequestDTO);

    @Mapping(source="member.username", target = "memberUsername")
    public abstract AppointmentReviewResponseDTO toReviewResponseDTO(Appointment appointment);

}
