package org.apollo.api.repository;

import org.apollo.api.model.Panel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PanelRepository extends JpaRepository<Panel, Long> {
}
