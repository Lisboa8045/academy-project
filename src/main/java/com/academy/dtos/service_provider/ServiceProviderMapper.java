package com.academy.dtos.service_provider;

import com.academy.dtos.appointment.AppointmentMapper;
import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = AppointmentMapper.class)
public abstract class ServiceProviderMapper {

    @Mappings({
            @Mapping(source= "provider.username", target = "memberName"),
            @Mapping(source= "service.id", target = "serviceId"),
            @Mapping(source = "permissions", target = "permissions", qualifiedByName = "mapPermissionsToEnums"),
            @Mapping(source = "appointmentList", target = "appointmentReviewList")
    })
    public abstract ServiceProviderResponseDTO toResponseDTO(ServiceProvider serviceProvider);

    @Mapping(target = "permissions", ignore = true)
    public abstract ServiceProvider toEntity(ServiceProviderRequestDTO ServiceProviderRequestDTO);


    /*@Named("enumToString")
    static String enumToString(ProviderPermission permission) {
        return permission.name();
    }
     */

    @Named("mapPermissionsToEnums")
    public List<ProviderPermissionEnum> mapPermissionsToEnums(List<ProviderPermission> permissions) {
        if(permissions == null)
            return null;

        return permissions.stream().map(ProviderPermission::getPermission).toList();
    }
}
