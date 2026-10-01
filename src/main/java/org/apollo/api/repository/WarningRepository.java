package org.apollo.api.repository;

import org.apollo.api.model.Warning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WarningRepository extends JpaRepository<Warning, Long>, JpaSpecificationExecutor<Warning> {

    @Override
    @EntityGraph(attributePaths = {"panelString", "panelString.inverter", "panel", "panel.panelString", "panel.panelString.inverter"})
    Page<Warning> findAll(Specification<Warning> spec, Pageable pageable);
}
