package org.apollo.api.repository;

import org.apollo.api.model.PanelOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PanelOverviewRepository extends JpaRepository<PanelOverview, Long>, JpaSpecificationExecutor<PanelOverview> {
}
