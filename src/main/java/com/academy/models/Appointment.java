package com.academy.models;

import com.academy.models.service.service_provider.ServiceProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity

@Table(name="appointment")

//Lombok annotations

@Getter @Setter

@NoArgsConstructor @ToString

public class Appointment {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long id;

    @ManyToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "member_id", nullable = true)

    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "service_provider_id", nullable = false)

    private ServiceProvider serviceProvider;

    @Column(name="rating")

    private Integer rating;

    @Column(name="comment")

    private String comment;

    @Column(name="created_at", updatable = false)

    @CreationTimestamp

    private LocalDateTime createdAt;

    @Column(name="updated_at")

    @UpdateTimestamp

    private LocalDateTime updatedAt;

}

