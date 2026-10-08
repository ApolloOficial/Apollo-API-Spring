package org.apollo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class MaintenancePartId implements Serializable {

    @Column(name = "maintenance_id")
    private Long maintenanceId;

    @Column(name = "part_id")
    private Long partId;

    public MaintenancePartId(Long maintenanceId, Long partId) {
        this.maintenanceId = maintenanceId;
        this.partId = partId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaintenancePartId other)) return false;
        return Objects.equals(maintenanceId, other.maintenanceId) && Objects.equals(partId, other.partId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maintenanceId, partId);
    }
}
