package org.apollo.api.repository;

import org.apollo.api.model.PanelPerformanceMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PanelPerformanceMeasurementRepository extends JpaRepository<PanelPerformanceMeasurement, Long> {
    List<PanelPerformanceMeasurement> findAllByPanelIdAndPanelBatchCompanyUnitCompanyId(Long panelId, Long companyId);
}