package com.powerconnect.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// One row of the "incidents" table.
// We keep the foreign keys as plain numbers (reportedBy, technicianId) to keep the mapping simple.
@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)   // the app can read it but never send it
    private Integer incidentId;

    @Column(name = "reported_by")
    private Integer reportedBy;

    @Column(name = "technician_id")
    private Integer technicianId;

    @Column(name = "service_type")
    private String serviceType;   // WATER, ELECTRICITY or SANITATION

    private String category;
    private String area;
    private String address;
    private String description;
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    private Integer rating;       // 1 to 5 stars, given when the community member closes it

    public Integer getIncidentId() { return incidentId; }
    public Integer getReportedBy() { return reportedBy; }
    public Integer getTechnicianId() { return technicianId; }
    public String getServiceType() { return serviceType; }
    public String getCategory() { return category; }
    public String getArea() { return area; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public Integer getRating() { return rating; }

    public void setReportedBy(Integer reportedBy) { this.reportedBy = reportedBy; }
    public void setTechnicianId(Integer technicianId) { this.technicianId = technicianId; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setCategory(String category) { this.category = category; }
    public void setArea(String area) { this.area = area; }
    public void setAddress(String address) { this.address = address; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public void setRating(Integer rating) { this.rating = rating; }
}
