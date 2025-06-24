package com.academy.dtos.appointment;


import com.academy.models.appointment.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public abstract class AppointmentMapper {

    @Mapping(source= "serviceProvider.id", target = "serviceProviderId")
    public abstract AppointmentResponseDTO toResponseDTO(Appointment appointment);

    @Mappings({
            @Mapping(source = "serviceProviderId", target = "serviceProvider.id"),
            @Mapping(source = "startDateTime", target = "startDateTime"),
            @Mapping(source = "endDateTime", target = "endDateTime"),
            @Mapping(source = "status", target = "status")
    })
    public abstract Appointment toEntity(AppointmentRequestDTO dto);

    @Mapping(source="member.username", target = "memberUsername")
    public abstract AppointmentReviewResponseDTO toReviewResponseDTO(Appointment appointment);
}