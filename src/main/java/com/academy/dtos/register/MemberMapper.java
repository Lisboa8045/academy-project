package com.academy.dtos.register;

import com.academy.models.Member;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class MemberMapper {

    public abstract Member toMember(RegisterRequestDto registerRequestDto);
}
