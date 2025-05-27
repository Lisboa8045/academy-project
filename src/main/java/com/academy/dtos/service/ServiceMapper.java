package com.academy.dtos.service;

import com.academy.models.Service;
import com.academy.models.Tag;
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

    @Mapping(source = "tagNames", target = "tags", qualifiedByName = "mapNamesToTags")
    @Mapping(expression = "java(memberRepository.findById(dto.ownerId()).orElseThrow())", target ="owner")
    public abstract Service toEntity(ServiceRequestDTO dto);

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
