package com.academy.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="tag")
@Getter
@Setter
public class Tag {

    @Column(name="tag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int tagId;

    @Column(name="tag_name")
    private String tagName;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
