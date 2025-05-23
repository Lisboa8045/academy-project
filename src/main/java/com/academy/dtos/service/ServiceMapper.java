package com.academy.dtos.service;

import com.academy.models.Service;
import com.academy.models.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ServiceMapper {

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "serviceType", ignore = true)
    public abstract Service toEntity(ServiceRequestDTO dto);

    @Mapping(source = "tags", target = "tagNames", qualifiedByName = "mapTagsToNames")
    @Mapping(source = "serviceType.name", target = "serviceTypeName")
    public abstract ServiceResponseDTO toDto(Service service);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "serviceType", ignore = true)
    public abstract void updateEntityFromDto(ServiceRequestDTO dto, @MappingTarget Service service);

    @Named("mapTagsToNames")
    protected List<String> mapTagsToNames(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }
}
