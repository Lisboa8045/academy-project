package com.academy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.academy.models.notification.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
