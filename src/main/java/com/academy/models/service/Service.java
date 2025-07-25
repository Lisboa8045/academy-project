package com.academy.models.service;

import com.academy.models.member.Member;
import com.academy.models.ServiceType;
import com.academy.models.Tag;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.models.shared.BaseEntity;
import com.academy.utils.FieldLengths;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="service")
@Getter
@Setter
@ToString(callSuper = true, exclude = {"owner", "serviceType", "tags", "serviceProviders"})
public class Service extends BaseEntity {

    @Column(name="name", length = FieldLengths.SERVICE_TITLE_MAX, nullable = false)
    private String name;

    @Lob
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

    @Column(name="entity", unique = true)
    private String entity;

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

    @OneToMany(mappedBy = "service")
    private List<ServiceProvider> serviceProviders = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceImages> images = new ArrayList<>();

}

