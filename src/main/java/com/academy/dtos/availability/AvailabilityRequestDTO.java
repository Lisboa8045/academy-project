package com.academy.dtos.availability;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.DayOfWeek;

@Getter
@Setter
public class AvailabilityRequestDTO {
    private long memberId;
    private DayOfWeek dayOfWeek;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}
