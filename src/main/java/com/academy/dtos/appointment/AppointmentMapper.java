package com.academy.dtos.appointment;


import com.academy.models.Appointment;
import com.academy.models.shared.BaseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class AppointmentMapper {
    @Mappings ({
            @Mapping(source= "serviceProvider", target = "serviceProviderId", qualifiedByName = "mapEntityIdOrDefault"),
            @Mapping(source= "member", target = "memberId", qualifiedByName = "mapEntityIdOrDefault")
    })

    public abstract AppointmentResponseDTO toResponseDTO(Appointment appointment);


    public abstract Appointment toEntity(AppointmentRequestDTO appointmentRequestDTO);

    @Mapping(source="member.username", target = "memberUsername")
    public abstract AppointmentReviewResponseDTO toReviewResponseDTO(Appointment appointment);

    @Named("mapEntityIdOrDefault")
    public Long mapEntityIdOrDefault(BaseEntity entity) {
        return entity != null ? entity.getId() : -1L;
    }

}
