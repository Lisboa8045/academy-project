package com.academy.dtos.appointment;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AppointmentRequestDTO {

    @NotNull
    private Long serviceProviderId;

    @NotNull
    private Long memberId;

    @Min(0)
    @Max(5)
    private Integer rating;

    @Size(max = 400)
    private String comment;
}
