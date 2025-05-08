package com.academy.models.service_provider;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// TODO Service, Member, Appointment

@Entity
@Table(name="service_provider")
@Getter
@Setter
@ToString
public class ServiceProvider {

    // TODO Might not make sense to have single id for service provider
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="user_service_id")
    private int serviceProviderId;

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
    private ProviderPermissions permission;
}
