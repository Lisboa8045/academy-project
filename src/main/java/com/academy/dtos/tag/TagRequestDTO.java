package com.academy.dtos.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagRequestDTO {

    @NotBlank(message = "Tag name is required")
    private String name;

    @NotNull(message = "Custom tag field must be set")
    private Boolean isCustom;
}