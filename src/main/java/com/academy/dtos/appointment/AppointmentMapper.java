package com.academy.dtos.appointment;


import com.academy.models.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public abstract class AppointmentMapper {
    @Mappings ({
            @Mapping(source= "serviceProvider.id", target = "serviceProviderId"),
            @Mapping(source= "member.id", target = "memberId"),
            @Mapping(source = "serviceProvider.provider.username", target = "serviceProviderUsername"),
            @Mapping(source = "member.username", target = "memberUsername"),
            @Mapping(source = "serviceProvider.service.name", target="serviceName")
    })
    public abstract AppointmentResponseDTO toResponseDTO(Appointment appointment);

    @Mappings ({
            @Mapping(source = "serviceProvider.provider.username", target = "serviceProviderUsername"),
            @Mapping(source = "member.username", target = "memberUsername"),
            @Mapping(source = "serviceProvider.service.name", target="serviceName")
    })
    public abstract AppointmentCardDTO toAppointmentCardDTO(Appointment appointment);

    public abstract Appointment toEntity(AppointmentRequestDTO appointmentRequestDTO);

    @Mapping(source="member.username", target = "memberUsername")
    public abstract AppointmentReviewResponseDTO toReviewResponseDTO(Appointment appointment);

}
