package com.academy.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="service")
@Getter
@Setter
public class Service {

    @Column(name="service_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int serviceId;

    @Column(name="price")
    private double price;

    @Column(name="discount")
    private int discount;

    @Column(name="is_negotiable")
    private boolean isNegotiable;

    @Column(name="duration")
    private int duration;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @ManyToMany
    @JoinTable(
            name = "service_tag",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<ServiceTag> serviceTags = new ArrayList<>();
}
