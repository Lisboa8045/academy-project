package com.academy.models.service_provider;

import com.academy.models.Member;
import com.academy.models.Service;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// TODO Appointment

@Entity
@Table(name="service_provider")
@Getter
@Setter
@ToString
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="service_provider_id")
    private long id;

    @OneToOne
    @JoinColumn(name="member_id")
    private Member provider;

    @OneToOne
    @JoinColumn(name="service_id")
    private Service service;

    @OneToMany
    @JoinColumn(name="appointment_id")
    private List<Appointment> appointmentList;

    @Enumerated(EnumType.STRING)
    @Column(name="provider_permission")
    private ProviderPermission permission;
}
