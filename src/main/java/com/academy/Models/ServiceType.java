package com.academy.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="service_type")
@Getter
@Setter
public class ServiceType {

    @Column(name="service_type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int serviceTypeId;

    @Column(name="service_type_name")
    private String serviceTypeName;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
