package com.academy.dtos.appointment;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentRequestDTO {
    private Long memberId;
    private Long serviceProviderId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String status;
    private Integer rating;
    private String comment;
}