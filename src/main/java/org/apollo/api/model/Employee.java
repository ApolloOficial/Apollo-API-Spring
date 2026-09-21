package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "company_unit_id")
    private UUID companyUnitId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "fcm_token", length = 300)
    private String fcmToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}