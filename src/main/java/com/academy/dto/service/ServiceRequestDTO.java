package com.academy.dto.service;

import com.academy.models.ServiceType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceRequestDTO {
    private String name;
    private String description;
    private double price;
    private int discount;
    private boolean isNegotiable;
    private int duration;
    private ServiceType serviceType;
    private List<Long> tagIds;
}
