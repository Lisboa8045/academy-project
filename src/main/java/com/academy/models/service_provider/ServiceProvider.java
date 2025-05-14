package com.academy.models.service_provider;

import com.academy.models.Appointment;
import com.academy.models.Member;
import com.academy.models.Service;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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

    @JsonBackReference
    @OneToMany(mappedBy = "serviceProvider")
    private List<Appointment> appointmentList;

    @Enumerated(EnumType.STRING)
    @Column(name="provider_permission")
    private ProviderPermission permission;

    @Column(name="created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
