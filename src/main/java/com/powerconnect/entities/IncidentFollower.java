package com.powerconnect.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// One row of "incident_followers": a community member who has the same problem as an
// incident someone else reported. They get the same alerts as the reporter.
@Entity
@Table(name = "incident_followers")
public class IncidentFollower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follower_id")
    private Integer followerId;

    @Column(name = "incident_id")
    private Integer incidentId;

    @Column(name = "user_id")
    private Integer userId;

    public IncidentFollower() { }

    public IncidentFollower(Integer incidentId, Integer userId) {
        this.incidentId = incidentId;
        this.userId = userId;
    }

    public Integer getFollowerId() { return followerId; }
    public Integer getIncidentId() { return incidentId; }
    public Integer getUserId() { return userId; }
}
