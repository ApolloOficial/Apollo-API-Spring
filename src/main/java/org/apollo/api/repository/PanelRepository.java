package org.apollo.api.repository;

import org.apollo.api.model.Panel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PanelRepository extends JpaRepository<Panel, Long> {
    List<Panel> findAllByBatchCompanyId(Long companyId);
    Optional<Panel> findByIdAndBatchCompanyId(Long id, Long companyId);
}
