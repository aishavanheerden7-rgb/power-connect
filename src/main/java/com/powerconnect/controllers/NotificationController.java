package com.powerconnect.controllers;

import com.powerconnect.entities.Notification;
import com.powerconnect.entities.User;
import com.powerconnect.repositories.NotificationRepository;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

// In-app alerts for every role:
//   community member -> updates on their incidents
//   technician       -> new jobs assigned to them
//   admin            -> new and reopened incidents
// The app checks GET /notifications/unread every 10 seconds to show new alerts without a refresh.
@CrossOrigin
@RestController
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final LoginCheck loginCheck;

    public NotificationController(NotificationRepository notificationRepository, LoginCheck loginCheck) {
        this.notificationRepository = notificationRepository;
        this.loginCheck = loginCheck;
    }

    @GetMapping("/notifications")
    public List<Notification> myNotifications(@RequestHeader(value = "Token", required = false) String token) {
        User user = loginCheck.user(token);
        return notificationRepository.findByUserIdOrderByCreatedAtDescNotificationIdDesc(user.getUserId());
    }

    // Just the number of unread alerts: small and quick, because the app asks every 10 seconds
    @GetMapping("/notifications/unread")
    public long unreadCount(@RequestHeader(value = "Token", required = false) String token) {
        User user = loginCheck.user(token);
        return notificationRepository.countByUserIdAndReadFalse(user.getUserId());
    }

    // Mark one alert as read (only if it belongs to the logged-in user)
    @PutMapping("/notifications/{id}/read")
    public void markRead(@RequestHeader(value = "Token", required = false) String token,
                         @PathVariable Integer id) {
        User user = loginCheck.user(token);
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null && notification.getUserId().equals(user.getUserId())) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
}
