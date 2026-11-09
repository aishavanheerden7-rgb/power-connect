package com.powerconnect.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// One row of "incident_updates": a status change with its time (the incident's timeline)
@Entity
@Table(name = "incident_updates")
public class IncidentUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "incident_id")
    private Integer incidentId;

    private String status;
    private String note;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // JPA needs an empty constructor
    public IncidentUpdate() { }

    public IncidentUpdate(Integer incidentId, String status, String note, Integer updatedBy) {
        this.incidentId = incidentId;
        this.status = status;
        this.note = note;
        this.updatedBy = updatedBy;
    }

    public Integer getUpdateId() { return updateId; }
    public Integer getIncidentId() { return incidentId; }
    public String getStatus() { return status; }
    public String getNote() { return note; }
    public Integer getUpdatedBy() { return updatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
