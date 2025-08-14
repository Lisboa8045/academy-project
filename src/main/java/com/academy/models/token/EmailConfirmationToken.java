package com.academy.models.token;

import com.academy.models.member.Member;
import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="email_confirmation_token")
@Getter
@Setter
public class EmailConfirmationToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name="raw_value", nullable = false)
    private String rawValue;

    @Column(name = "encoded_value", nullable = false)
    private String encondedValue;

    @Column(name = "expiration_date", updatable = false)
    protected LocalDateTime expirationDate;

}
