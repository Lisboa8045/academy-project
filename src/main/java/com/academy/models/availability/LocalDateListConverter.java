package com.academy.models.availability;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class LocalDateListConverter implements AttributeConverter<List<LocalDate>, String> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public String convertToDatabaseColumn(List<LocalDate> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";
        return attribute.stream().map(formatter::format).collect(Collectors.joining(","));
    }

    @Override
    public List<LocalDate> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new ArrayList<>();
        return Arrays.stream(dbData.split(","))
                .map(LocalDate::parse)
                .collect(Collectors.toList());
    }
}
