package com.academy.models.service;

import com.academy.models.Member;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.models.service.service_provider.ServiceProvider;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@ToString(exclude = "serviceProviders")
public class Service {

    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @Column(name="name")
    private String name;

    @Column(name="description")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    @Column(name="price")
    private double price;

    @Column(name="discount")
    private int discount;

    @Column(name="negotiable")
    private boolean negotiable = false; // Default value

    @Column(name="duration")
    private int duration;

    @Column(name="created_at", updatable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "service_tag",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"service_id", "tag_id"})
            }
    )
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceProvider> serviceProviders = new ArrayList<>();
}
