package com.academy.dto.service;

import com.academy.models.ServiceType;
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

    @Min(0)
    @Max(100)
    private int discount;

    private boolean isNegotiable = false; // Default value

    @Positive
    private int duration;

    @NotNull
    private ServiceType serviceType;

    private List<Long> tagIds;
}
