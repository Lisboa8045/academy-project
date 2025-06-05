package com.academy.dtos.register;

import com.academy.dtos.member.MemberResponseDTO;
import com.academy.models.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class MemberMapper {

    public abstract Member toMember(RegisterRequestDto registerRequestDto);

    @Mapping(target = "role", source = "role.name")
    public abstract MemberResponseDTO toResponseDTO(Member member);
}