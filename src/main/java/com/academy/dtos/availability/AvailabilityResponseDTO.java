package com.academy.dtos.availability;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;



@Getter
@Setter
public class AvailabilityResponseDTO {
    
    @NotEmpty
    private long id;

    @NotEmpty
    private long memberId;

    @NotEmpty
    private DayOfWeek dayOfWeek;

    @NotEmpty
    @FutureOrPresent(message = "End date and time must be in the future or present")
    private LocalDateTime startDateTime;

    @NotEmpty
    @FutureOrPresent(message = "End date and time must be in the future or present")
    private LocalDateTime endDateTime;
}
    
