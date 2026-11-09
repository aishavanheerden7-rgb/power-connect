package com.powerconnect.controllers;

import com.powerconnect.entities.Incident;
import com.powerconnect.entities.IncidentFollower;
import com.powerconnect.entities.Notification;
import com.powerconnect.entities.User;
import com.powerconnect.repositories.IncidentFollowerRepository;
import com.powerconnect.repositories.NotificationRepository;
import com.powerconnect.repositories.UserRepository;
import org.springframework.stereotype.Component;

// Creates in-app alerts (rows in the notifications table).
// Several controllers send alerts, so the code lives here once instead of being copied.
@Component
public class AlertSender {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final IncidentFollowerRepository followerRepository;

    public AlertSender(NotificationRepository notificationRepository,
                       UserRepository userRepository,
                       IncidentFollowerRepository followerRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.followerRepository = followerRepository;
    }

    // One alert to one person
    public void toUser(Integer userId, Integer incidentId, String status, String message) {
        notificationRepository.save(new Notification(userId, incidentId, status, message));
    }

    // The community member who reported it, plus everyone who follows it (same problem)
    public void toCommunity(Incident incident, String status, String message) {
        toUser(incident.getReportedBy(), incident.getIncidentId(), status, message);
        // Followers cannot confirm the work (only the reporter can), so they get their own "completed" wording
        String followerStatus = status.equals("COMPLETED") ? "COMPLETED_FOLLOWER" : status;
        for (IncidentFollower follower : followerRepository.findByIncidentId(incident.getIncidentId())) {
            toUser(follower.getUserId(), incident.getIncidentId(), followerStatus, message);
        }
    }

    // Every admin
    public void toAdmins(Integer incidentId, String status, String message) {
        for (User admin : userRepository.findByRole("ADMIN")) {
            toUser(admin.getUserId(), incidentId, status, message);
        }
    }

    // The councillor(s) for the incident's area
    public void toCouncillors(Incident incident, String status, String message) {
        for (User councillor : userRepository.findByRoleAndArea("COUNCILLOR", incident.getArea())) {
            toUser(councillor.getUserId(), incident.getIncidentId(), status, message);
        }
    }
}
