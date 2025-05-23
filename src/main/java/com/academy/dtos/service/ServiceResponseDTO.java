package com.academy.dtos.service;

import com.academy.models.ServiceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ServiceResponseDTO {
    private Long id;
    private String name;
    private String description;
    private long ownerId;
    private double price;
    private int discount;
    private boolean isNegotiable;
    private int duration;
    private ServiceType serviceType;
    private List<String> tagNames;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
