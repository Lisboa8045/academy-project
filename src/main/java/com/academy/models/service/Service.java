package com.academy.models.service;

import com.academy.models.Member;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.models.service.service_provider.ServiceProvider;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.EAGER)
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

    public void removeAllTags() {
        for (Tag tag : new ArrayList<>(tags)) {
            tag.getServices().remove(this); // Remove this service from each associated tag
        }
        tags.clear(); // Clear the local list
    }

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceProvider> serviceProviders;


}
