package com.academy.models.log;

import com.academy.models.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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
    @JoinColumn(name="member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name="action", nullable = false)
    private ActionEnum action;


    // column definition -> + de 255 caracteres
    @Column(name="details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    //LOB -> large object como texto longo e ficheiros binários
    @Lob
    @Column(name = "attachemnt")
    private byte[] attachment;
    
    // nao tem updated_at porque este campo nunca é alterado
}
