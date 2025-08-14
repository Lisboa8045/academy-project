package com.academy.dtos.service_provider;

import com.academy.dtos.appointment.AppointmentMapper;
import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import com.academy.models.service.Service;
import com.academy.models.member.Member;

import java.util.List;

@Mapper(componentModel = "spring", uses = AppointmentMapper.class)
public abstract class ServiceProviderMapper {

    @Mappings({
            @Mapping(source= "provider", target = "memberName", qualifiedByName = "mapServiceProviderNameOrDefault"),
            @Mapping(source = "service", target = "serviceId", qualifiedByName = "mapServiceIdOrDefault"),
            @Mapping(source = "permissions", target = "permissions", qualifiedByName = "mapPermissionsToEnums"),
            @Mapping(source = "appointmentList", target = "appointmentReviewList")
    })
    public abstract ServiceProviderResponseDTO toResponseDTO(ServiceProvider serviceProvider);

    @Mapping(target = "permissions", ignore = true)
    public abstract ServiceProvider toEntity(ServiceProviderRequestDTO serviceProviderRequestDTO);

    @Named("mapPermissionsToEnums")
    public List<ProviderPermissionEnum> mapPermissionsToEnums(List<ProviderPermission> permissions) {
        if(permissions == null)
            return null;

        return permissions.stream().map(ProviderPermission::getPermission).toList();
    }

    @Named("mapServiceIdOrDefault")
    public Long mapServiceIdOrDefault(Service service) {
        return service != null ? service.getId() : -1L;
    }

    @Named("mapServiceProviderNameOrDefault")
    public String mapServiceProviderNameOrDefault(Member member) {
        return member != null ? member.getUsername() : "Deleted User";
    }
}
