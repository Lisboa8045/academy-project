package com.academy.dtos.availability;

import java.time.LocalDate;
import java.util.List;

public record DaySchedule(LocalDate date, List<DateTimeRange> timeRanges) {}

