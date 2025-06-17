package com.academy.models;

import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.models.shared.BaseEntity;
import com.academy.util.FieldLengths;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;

    @Column(name="rating")
    private Integer rating;

    @Column(name="comment", length = FieldLengths.REVIEW_MAX)
    private String comment;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "status")
    private String status;

}