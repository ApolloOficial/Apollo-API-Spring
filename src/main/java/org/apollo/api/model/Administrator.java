package org.apollo.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrator")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Roles role;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 150)
    private String password;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}