package com.powerconnect.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// One row of "notifications": an in-app alert for a community member about one of their incidents
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "incident_id")
    private Integer incidentId;

    private String status;
    private String message;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Notification() { }

    public Notification(Integer userId, Integer incidentId, String status, String message) {
        this.userId = userId;
        this.incidentId = incidentId;
        this.status = status;
        this.message = message;
    }

    public Integer getNotificationId() { return notificationId; }
    public Integer getUserId() { return userId; }
    public Integer getIncidentId() { return incidentId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRead(boolean read) { this.read = read; }
}
