package org.apollo.api.repository;

import org.apollo.api.model.MaintenancePart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MaintenancePartRepository extends JpaRepository<MaintenancePart, org.apollo.api.model.MaintenancePartId> {

    @EntityGraph(attributePaths = {"part"})
    @Query("SELECT mp FROM MaintenancePart mp WHERE mp.id.maintenanceId = :maintenanceId ORDER BY mp.consumedAt")
    List<MaintenancePart> findByMaintenanceId(@Param("maintenanceId") Long maintenanceId);

    @Query("SELECT mp FROM MaintenancePart mp WHERE mp.id.maintenanceId IN :ids")
    List<MaintenancePart> findByMaintenanceIds(@Param("ids") Collection<Long> ids);
}
