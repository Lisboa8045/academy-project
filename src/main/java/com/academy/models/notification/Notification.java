package com.academy.models.notification;

import com.academy.models.member.Member;
import com.academy.models.shared.BaseEntity;
import com.academy.util.FieldLengths;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="notification")
@Getter
@Setter
@ToString(callSuper = true, exclude="member")
public class Notification extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name="title", length = FieldLengths.NOTIFICATION_TITLE_MAX)
    private String title;

    @Column(name="seen")
    private boolean seen;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationTypeEnum notificationTypeEnum;

}