package com.academy.models.availability;

import com.academy.models.shared.BaseEntity;
import com.academy.models.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "availability")
public class Availability extends BaseEntity {

    @OneToMany(mappedBy = "availability", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberAvailability> memberAvailabilities = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "is_exception")
    private boolean isException;

    @Column(name = "dates")
    private String datesString;

    @Transient
    private List<LocalDate> dates;

    public void setDates(List<LocalDate> dates) {
        this.dates = dates;
        this.datesString = datesToString(dates);
    }

    public List<LocalDate> getDates() {
        if (dates == null && datesString != null) {
            dates = stringToDates(datesString);
        }
        return dates;
    }

    private String datesToString(List<LocalDate> list) {
        return list.stream().map(LocalDate::toString).collect(Collectors.joining(","));
    }

    private List<LocalDate> stringToDates(String str) {
        return Arrays.stream(str.split(",")).map(LocalDate::parse).toList();
    }
}