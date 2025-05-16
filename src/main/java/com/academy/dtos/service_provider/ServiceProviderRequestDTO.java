package com.academy.dtos.service_provider;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ServiceProviderRequestDTO {


    @NotNull
    private Long memberId;

    @NotNull
    private Long serviceId;

    @Min(0)
    @Max(3)
    private Integer permission;

}
