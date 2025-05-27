package com.academy.dtos.service;

import com.academy.models.service.Service;
import com.academy.models.Tag;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.TagRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ServiceMapper {

    protected TagRepository tagRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Mapping(source = "dto.tagNames", target = "tags", qualifiedByName = "mapNamesToTags")
    @Mapping(expression = "java(memberRepository.findById(ownerId).orElseThrow())", target ="owner")
    public abstract Service toEntity(ServiceRequestDTO dto, Long ownerId);

    @Mapping(source = "service.tags", target = "tagNames", qualifiedByName = "mapTagsToNames")
    @Mapping(expression = "java(service.getOwner().getId())", target = "ownerId")
    public abstract ServiceResponseDTO toDto(Service service, List<ProviderPermissionEnum> permissions);

    @Mapping(source = "tags", target = "tagNames", qualifiedByName = "mapTagsToNames")
    @Mapping(expression = "java(service.getOwner().getId())", target = "ownerId")
    public abstract ServiceResponseDTO toDto(Service service);

    @Named("mapNamesToTags")
    protected List<Tag> mapNamesToTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return null;
        }
        return tagRepository.findAllByNameIn(tagNames);
    }

    @Named("mapTagsToNames")
    protected List<String> mapTagsToNames(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

}
