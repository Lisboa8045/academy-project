package com.academy.models.availability;

import com.academy.models.member.Member;
import com.academy.models.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Table(name = "member_availability")
public class MemberAvailability extends BaseEntity {

    @ManyToOne
    private Member member;

    @ManyToOne
    private Availability availability;

    @Column(name = "dates_string")
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
