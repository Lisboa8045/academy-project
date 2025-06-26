package com.academy.models.service.service_provider;

import com.academy.models.appointment.Appointment;

import com.academy.models.member.Member;

import com.academy.models.service.Service;

import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import lombok.Getter;

import lombok.Setter;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@Entity

@Table(name="service_provider",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "service_id"}))
@Getter
@Setter
@ToString(callSuper = true, exclude = {"provider", "service", "appointmentList", "permissions"})
public class ServiceProvider extends BaseEntity {

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

    @Column(name  = "active", nullable = false)
    private boolean active = true;

}

