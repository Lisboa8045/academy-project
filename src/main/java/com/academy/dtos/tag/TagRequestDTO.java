package com.academy.dtos.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TagRequestDTO {

    @NotBlank
    private String name;

    @NotNull
    private Boolean isCustom;

    private List<Long> serviceIds;
}