package com.academy.dtos.appointment;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentResponseDTO {
    private int id;
    private Long memberId;
    private Long serviceProviderId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String status;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}