package com.academy.dtos.availability;

import com.academy.models.availability.MemberAvailability;
import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class AvailabilityMapper {

    public List<DaySchedule> toDaySchedules(List<MemberAvailability> memberAvailabilities) {
        // Mapa que associa cada data a uma lista de DateTimeRanges
        Map<LocalDate, List<DateTimeRange>> map = new HashMap<>();

        for (MemberAvailability ma : memberAvailabilities) {
            var availability = ma.getAvailability();
            LocalTime start = availability.getStartTime();
            LocalTime end = availability.getEndTime();
            DateTimeRange range = new DateTimeRange(start, end);

            for (LocalDate date : ma.getDates()) {
                map.computeIfAbsent(date, k -> new ArrayList<>()).add(range);
            }
        }

        // Converter o mapa em DaySchedule
        return map.entrySet().stream()
                .map(entry -> new DaySchedule(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DaySchedule::date)) // opcional: ordenar por data
                .collect(Collectors.toList());
    }
}