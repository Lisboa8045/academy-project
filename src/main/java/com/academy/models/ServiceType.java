package com.academy.models;

import com.academy.models.service.Service;
import com.academy.util.FieldLengths;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="service_type")
@Getter
@Setter
@ToString(exclude="services")
public class ServiceType {

    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @NotBlank
    @Column(name="name", unique = true, length = FieldLengths.SERVICE_TYPE_MAX)
    private String name;

    @NotBlank
    @Column(name="icon", length = FieldLengths.URL_MAX)
    private String icon;

    @OneToMany(mappedBy = "serviceType", fetch = FetchType.LAZY)
    private List<Service> services = new ArrayList<>();

    @Column(name="created_at", updatable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
