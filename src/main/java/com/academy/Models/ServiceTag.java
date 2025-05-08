package com.academy.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="service_tag")
@Getter
@Setter
public class ServiceTag {

    @Column(name="service_tag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int serviceTagId;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Column(name="tag_name")
    private String tagName;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
