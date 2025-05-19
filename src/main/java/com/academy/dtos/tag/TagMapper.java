package com.academy.dtos.tag;

import com.academy.models.Service;
import com.academy.models.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class TagMapper {

    @Mapping(source = "services", target = "serviceIds", qualifiedByName = "mapServicesToIds")
    public abstract TagResponseDTO toDto(Tag tag);

    public abstract Tag toEntity(TagRequestDTO dto);

    @Named("mapServicesToIds")
    protected List<Long> mapServicesToIds(List<Service> services) {
        if (services == null || services.isEmpty()) {
            return null;
        }
        return services.stream()
                .map(Service::getId)
                .collect(Collectors.toList());
    }
}