package com.academy.dtos.service;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Positive
    private double price;

    @Min(value = 0, message = "cannot be less than 0%")
    @Max(value = 100, message = "cannot exceed 100%")
    private int discount;

    private boolean isNegotiable = false; // Default value

    @Positive
    private int duration;

    @NotNull
    private Long serviceTypeId;

    private List<String> tagNames;
}
