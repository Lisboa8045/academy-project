package com.academy.models.log;

import com.academy.models.member.Member;
import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="log")
@Getter
@Setter
@ToString(callSuper = true, exclude = {"member", "attachment"})
public class Log extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name="action", nullable = false)
    private ActionEnum action;

    // column definition -> + de 255 caracteres
    @Column(name="details", columnDefinition = "TEXT")
    private String details;

    //LOB -> large object como texto longo e ficheiros binários
    @Lob
    @Column(name = "attachemnt")
    private byte[] attachment;

}
