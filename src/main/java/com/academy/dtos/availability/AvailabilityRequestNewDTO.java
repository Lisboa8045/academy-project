package com.academy.dtos.availability;

import java.util.List;

public record AvailabilityRequestNewDTO(
        List<DaySchedule> daySchedules
) {}
