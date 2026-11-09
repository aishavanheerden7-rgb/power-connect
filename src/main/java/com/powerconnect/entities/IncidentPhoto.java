package com.powerconnect.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// One row of "incident_photos": a picture stored as base64 text ("data:image/jpeg;base64,...")
@Entity
@Table(name = "incident_photos")
public class IncidentPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Integer photoId;

    @Column(name = "incident_id")
    private Integer incidentId;

    @Column(name = "uploaded_by")
    private Integer uploadedBy;

    private String image;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public IncidentPhoto() { }

    public IncidentPhoto(Integer incidentId, Integer uploadedBy, String image) {
        this.incidentId = incidentId;
        this.uploadedBy = uploadedBy;
        this.image = image;
    }

    public Integer getPhotoId() { return photoId; }
    public Integer getIncidentId() { return incidentId; }
    public Integer getUploadedBy() { return uploadedBy; }
    public String getImage() { return image; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
