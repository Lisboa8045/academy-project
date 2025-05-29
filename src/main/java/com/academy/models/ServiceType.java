package com.academy.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="service_type")
@Getter
@Setter
@ToString
public class ServiceType {

    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @NotBlank
    @Column(name="name")
    private String name;

    @NotBlank
    @Column(name="icon")
    private String icon;

    @Column(name="created_at", updatable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
