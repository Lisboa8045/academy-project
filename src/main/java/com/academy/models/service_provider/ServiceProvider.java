package com.academy.models.service_provider;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="service_provider")
@Getter
@Setter
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

//    @OneToOne
//    @JoinColumn(name="role_id")
//    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name="provider_permission")
    private ProviderPermissions permission;
}
