package com.academy.dtos.tag;

import com.academy.models.Service;
import com.academy.models.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class TagMapper {

    @Mapping(target = "services", ignore = true)
    public abstract Tag toEntity(TagRequestDTO dto);

    @Mapping(source = "services", target = "serviceIds", qualifiedByName = "mapServicesToIds")
    public abstract TagResponseDTO toDto(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "services", ignore = true)
    public abstract void updateEntityFromDto(TagRequestDTO dto, @MappingTarget Tag tag);

    @Named("mapServicesToIds")
    protected List<Long> mapServicesToIds(List<Service> services) {
        if (services == null || services.isEmpty()) {
            return List.of();
        }
        return services.stream()
                .map(Service::getId)
                .collect(Collectors.toList());
    }

}