package org.apollo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "Maintenance_Technician")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceTechnician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "co_unity_id", nullable = false)
    private Integer coUnityId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "password", nullable = false, length = 150)
    private String password;

    @Column(name = "phone", nullable = false, unique = true, length = 11)
    private String phone;

    @Column(name = "profile_pic")
    private String profilePic;

    @Column(name = "job_registration", nullable = false, unique = true, length = 50)
    private String jobRegistration;
}