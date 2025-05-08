package com.academy.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="tag")
@Getter
@Setter
@ToString
public class Tag {

    @Column(name="tag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long tagId;

    @Column(name="tag_name", unique = true)
    private String tagName;

    @Column(name="is_custom")
    private Boolean isCustom;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "tags")
    private List<Service> services;
}
