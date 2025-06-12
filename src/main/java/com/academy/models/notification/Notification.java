package com.academy.models.notification;

import com.academy.models.Member;
import com.academy.models.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="notification")
@Getter
@Setter
public class Notification extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name="title")
    private String title;

    @Column(name="seen")
    private boolean seen;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationTypeEnum notificationTypeEnum;

}
