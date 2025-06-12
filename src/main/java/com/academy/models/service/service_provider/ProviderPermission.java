package com.academy.models.service.service_provider;

import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="provider_permission")
@Getter
@Setter
public class ProviderPermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;

    @Enumerated(EnumType.STRING)
    @Column(name="provider_permission", nullable = false, length = 200)
    private ProviderPermissionEnum permission;

}
