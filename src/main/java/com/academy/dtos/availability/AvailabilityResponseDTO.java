package com.academy.dtos.availability;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;



@Getter
@Setter
public class AvailabilityResponseDTO {
    @NotNull
    private long id;

    @NotNull
    private long memberId;

    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull
    @FutureOrPresent(message = "End date and time must be in the future or present")
    private LocalDateTime startDateTime;

    @NotNull
    @FutureOrPresent(message = "End date and time must be in the future or present")
    private LocalDateTime endDateTime;
}
    
