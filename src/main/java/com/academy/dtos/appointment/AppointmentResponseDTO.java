package com.academy.dtos.appointment;

import lombok.Data;

@Data
public class AppointmentResponseDTO {
    private int id;
    private Long serviceProviderId;
    private Long memberId;
    private Integer rating;
    private String comment;
}
