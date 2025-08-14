package com.academy.models.appointment;

import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.models.shared.BaseEntity;
import com.academy.utils.FieldLengths;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity

@Table(name="appointment")

//Lombok annotations

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = {"member", "serviceProvider"})
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;

    @Column(name="rating")
    private Integer rating = 0;

    @Column(name="comment", length = FieldLengths.REVIEW_MAX)
    private String comment;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status;

    @Column(name="price")
    private double price;
}

