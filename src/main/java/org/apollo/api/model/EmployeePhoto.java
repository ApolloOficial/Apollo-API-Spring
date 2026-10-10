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
@Table(name = "employee_photo")
@Getter
@Setter
@NoArgsConstructor
public class EmployeePhoto {

    @Id
    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public EmployeePhoto(UUID employeeId, String contentType, byte[] data) {
        this.employeeId = employeeId;
        this.contentType = contentType;
        this.data = data;
    }
}
