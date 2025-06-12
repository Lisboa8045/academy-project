package com.academy.models;

import com.academy.models.service.Service;
import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="service_type")
@Getter
@Setter
@ToString(exclude="services")
public class ServiceType extends BaseEntity {

    @NotBlank
    @Column(name="name", unique = true)
    private String name;

    @NotBlank
    @Column(name="icon")
    private String icon;

    @OneToMany(mappedBy = "serviceType", fetch = FetchType.LAZY)
    private List<Service> services = new ArrayList<>();

}
