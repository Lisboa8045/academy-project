package com.academy.models.service.service_provider;

import com.academy.models.Appointment;

import com.academy.models.member.Member;

import com.academy.models.service.Service;

import jakarta.persistence.*;

import lombok.Getter;

import lombok.Setter;

import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp;

import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;


@Entity

@Table(name="service_provider",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "service_id"}))
@Getter
@Setter
@ToString(exclude = "service")
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="service_provider_id")
    private long id;

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member provider;

    @ManyToOne
    @JoinColumn(name="service_id")
    private Service service;

    @OneToMany(mappedBy = "serviceProvider", fetch = FetchType.EAGER)
    private List<Appointment> appointmentList = new ArrayList<>();

    @OneToMany(mappedBy = "serviceProvider")
    private List<ProviderPermission> permissions = new ArrayList<>();

    @Column(name="created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}

