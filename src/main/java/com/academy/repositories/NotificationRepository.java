package com.academy.repositories;

import com.academy.models.Member;
import com.academy.models.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMember(Member member);
    List<Notification> findByMemberAndSeen(Member member, boolean seen);
}
