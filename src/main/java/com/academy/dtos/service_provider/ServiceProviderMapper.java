package com.academy.dtos.service_provider;

import com.academy.dtos.appointment.AppointmentMapper;
import com.academy.models.service_provider.ProviderPermission;
import com.academy.models.service_provider.ServiceProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = AppointmentMapper.class)
public abstract class ServiceProviderMapper {

    @Mappings({
            @Mapping(source= "provider.username", target = "memberName"),
            @Mapping(source= "service.id", target = "serviceId"),
            @Mapping(source = "permission", target = "permission", qualifiedByName = "enumToString"),
            @Mapping(source = "appointmentList", target = "appointmentReviewList")
    })
    public abstract ServiceProviderResponseDTO toResponseDTO(ServiceProvider serviceProvider);

    public abstract ServiceProvider toEntity(ServiceProviderRequestDTO ServiceProviderRequestDTO);


    @Named("enumToString")
    static String enumToString(ProviderPermission permission) {
        return permission.name();
    }
}
