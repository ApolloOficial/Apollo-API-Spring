package org.apollo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class StockId implements Serializable {

    @Column(name = "company_unit_id")
    private UUID companyUnitId;

    @Column(name = "part_id")
    private Long partId;

    public StockId(UUID companyUnitId, Long partId) {
        this.companyUnitId = companyUnitId;
        this.partId = partId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockId other)) return false;
        return Objects.equals(companyUnitId, other.companyUnitId) && Objects.equals(partId, other.partId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyUnitId, partId);
    }
}
