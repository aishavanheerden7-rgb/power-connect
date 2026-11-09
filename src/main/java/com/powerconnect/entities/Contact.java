package com.powerconnect.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// One row of "contacts": a service hotline (area is null) or the councillor for an area
@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)   // the app can read it but never send it
    private Integer contactId;

    private String name;
    private String title;

    @Column(name = "service_type")
    private String serviceType;

    private String area;
    private String phone;
    private String email;

    public Integer getContactId() { return contactId; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getServiceType() { return serviceType; }
    public String getArea() { return area; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = name; }
    public void setTitle(String title) { this.title = title; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setArea(String area) { this.area = area; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
}
