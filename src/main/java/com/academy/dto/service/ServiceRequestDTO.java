package com.academy.dto.service;

import com.academy.models.ServiceType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceRequestDTO {

    @NotBlank(message = "Service name is required")
    private String name;

    @NotBlank(message = "Description must not be empty")
    private String description;

    @Positive(message = "Price must be greater than zero")
    private double price;

    @Min(value = 0, message = "Discount cannot be less than 0%")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    private int discount;

    private boolean isNegotiable = false; // Default value

    @Positive(message = "Invalid duration, must be a positive number in minutes")
    private int duration;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    private List<String> tagNames;
}
