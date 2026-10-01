package org.apollo.api.repository;

import org.apollo.api.model.PanelString;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PanelStringRepository extends JpaRepository<PanelString, UUID> {

    @EntityGraph(attributePaths = {"inverter", "inverter.companyUnit", "panelModel"})
    Optional<PanelString> findByIdAndInverterCompanyUnitCompanyId(UUID id, Long companyId);
}
