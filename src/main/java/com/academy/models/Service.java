package com.academy.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="service")
@Getter
@Setter
@ToString
public class Service {

    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @NotBlank
    @Column(name="name")
    private String name;

    @NotBlank
    @Column(name="description")
    private String description;

    @Positive
    @Column(name="price")
    private double price;

    @Min(0)
    @Max(100)
    @Column(name="discount")
    private int discount;

    @Column(name="is_negotiable")
    private boolean isNegotiable = false; // Default value

    @Positive
    @Column(name="duration")
    private int duration;

    @Column(name="created_at", updatable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "service_type_id", nullable = false)
    @NotNull
    private ServiceType serviceType;

    @ManyToMany
    @JoinTable(
            name = "service_tag",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();
}
