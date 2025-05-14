package com.academy.models.notification;

import com.academy.models.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;

@Entity
@Table(name="notification")
@Getter @Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="notification_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name="title")
    private String title;

    @Column(name="seen")
    private boolean seen;

    @Column(name="created_at")
    @CreationTimestamp
    private LocalTime createdAt;

    @Column(name="update_at")
    @UpdateTimestamp
    private LocalTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationTypeEnum notificationTypeEnum;


}
