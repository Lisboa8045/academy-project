package com.academy.models.service.service_provider;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="provider_permission")
@Getter
@Setter
@ToString(exclude="serviceProvider")
public class ProviderPermission {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;

    @Enumerated(EnumType.STRING)
    @Column(name="provider_permission", nullable = false, length = 200)
    private ProviderPermissionEnum permission;

}
