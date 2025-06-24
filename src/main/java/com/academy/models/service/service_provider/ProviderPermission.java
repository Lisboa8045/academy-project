package com.academy.models.service.service_provider;

import com.academy.models.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="provider_permission")
@Getter
@Setter
@ToString(callSuper = true, exclude="serviceProvider")
public class ProviderPermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;

    @Enumerated(EnumType.STRING)
    @Column(name="provider_permission", nullable = false, length = 200)
    private ProviderPermissionEnum permission;

}
