package com.powerconnect.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// One row of the "users" table
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_code")
    private String userCode;

    private String role;        // ADMIN, TECHNICIAN, COUNCILLOR or COMMUNITY

    @Column(name = "full_name")
    private String fullName;

    private String email;
    private String phone;
    private String area;

    @Column(name = "service_type")
    private String serviceType; // technicians only: WATER, ELECTRICITY or SANITATION

    private String language;    // en, bem or ny

    @JsonIgnore                 // never send the password hash to the app
    @Column(name = "password_hash")
    private String passwordHash;

    @JsonIgnore                 // the token is only sent once, in the login reply
    private String token;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getUserId() { return userId; }
    public String getUserCode() { return userCode; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getArea() { return area; }
    public String getServiceType() { return serviceType; }
    public String getLanguage() { return language; }
    public String getPasswordHash() { return passwordHash; }
    public String getToken() { return token; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUserCode(String userCode) { this.userCode = userCode; }
    public void setRole(String role) { this.role = role; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setArea(String area) { this.area = area; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setLanguage(String language) { this.language = language; }
    public void setToken(String token) { this.token = token; }
}
