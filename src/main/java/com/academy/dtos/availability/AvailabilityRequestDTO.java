package com.academy.dtos.availability;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.AssertTrue;	

@Getter
@Setter
public class AvailabilityRequestDTO {
    @NotNull(message = "Member ID cannot be null")
    private long memberId;

    @NotNull(message = "Day of the week cannot be null")
    private DayOfWeek dayOfWeek;
    
    @NotNull(message = "Start date and time cannot be null")
    @FutureOrPresent(message = "Start date and time must be in the future or present")
    private LocalDateTime startDateTime;
    
    @NotNull(message = "End date and time cannot be null")
    @FutureOrPresent(message = "End date and time must be in the future or present")
    @AssertTrue(message = "End date and time must be after start date and time")
    private LocalDateTime endDateTime;
}
