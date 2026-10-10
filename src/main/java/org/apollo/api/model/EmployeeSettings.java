package org.apollo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_settings")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeSettings {

    @Id
    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "language", nullable = false, length = 10)
    private String language = "pt-BR";

    @Column(name = "notify_predictive", nullable = false)
    private Boolean notifyPredictive = true;

    @Column(name = "notify_panel_alert", nullable = false)
    private Boolean notifyPanelAlert = true;

    @Column(name = "notify_email", nullable = false)
    private Boolean notifyEmail = false;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public EmployeeSettings(UUID employeeId) {
        this.employeeId = employeeId;
    }
}
