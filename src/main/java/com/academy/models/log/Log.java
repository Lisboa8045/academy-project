package com.academy.models.log;

import com.academy.models.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="log")
@Getter @Setter

public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="log_id")
    private long id;

    @ManyToOne
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name="action", nullable = false)
    private ActionEnum action;


    // column definition -> + de 255 caracteres
    @Column(name="details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    // nao tem updated_at porque este campo nunca é alterado



}
