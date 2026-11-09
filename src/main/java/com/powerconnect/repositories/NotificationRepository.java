package com.powerconnect.repositories;

import com.powerconnect.entities.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderByCreatedAtDescNotificationIdDesc(Integer userId);  // newest first (by number if the same second)

    long countByUserIdAndReadFalse(Integer userId);                         // how many unread alerts
}
