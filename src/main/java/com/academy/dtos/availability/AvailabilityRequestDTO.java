package com.academy.dtos.availability;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.DayOfWeek;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;	

@Getter
@Setter
public class AvailabilityRequestDTO {

    @NotBlank
    private long id;

    @NotBlank
    private long memberId;

    @NotBlank
    private DayOfWeek dayOfWeek;
    
    @NotBlank
    @FutureOrPresent(message = "Start date and time must be in the future or present")
    private LocalDateTime startDateTime;
    
    @NotBlank
    @FutureOrPresent(message = "End date and time must be in the future or present")
    private LocalDateTime endDateTime;

    @AssertTrue(message = "End date and time must be after start date and time")
    private boolean validate() {
        if (startDateTime != null && endDateTime != null) {
            return endDateTime.isAfter(startDateTime); 
        }
        return true; 
    }
}
