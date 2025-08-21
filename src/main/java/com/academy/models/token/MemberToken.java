package com.academy.models.token;

import com.academy.models.member.Member;
import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="member_token")
@Getter
@Setter
public class MemberToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name="raw_value", nullable = false)
    private String rawValue;

    @Column(name = "encoded_value", nullable = false)
    private String encodedValue;

    @Column(name = "expiration_date", updatable = false)
    protected LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenTypeEnum tokenType;
}
