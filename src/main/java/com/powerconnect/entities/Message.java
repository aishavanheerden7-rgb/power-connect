package com.powerconnect.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// One row of "messages": a short message written on an incident
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "incident_id")
    private Integer incidentId;

    @Column(name = "sender_id")
    private Integer senderId;

    private String body;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Message() { }

    public Message(Integer incidentId, Integer senderId, String body) {
        this.incidentId = incidentId;
        this.senderId = senderId;
        this.body = body;
    }

    public Integer getMessageId() { return messageId; }
    public Integer getIncidentId() { return incidentId; }
    public Integer getSenderId() { return senderId; }
    public String getBody() { return body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
